package com.work.javafx.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

public class AutoUpdater {

    private static final String LOCAL_VERSION = getLocalVersion();
    private static final String VERSION_JSON_URL = getVersionJsonUrl();

    private static String getVersionJsonUrl() {
        try (InputStream in = AutoUpdater.class.getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version.json.url");
        } catch (IOException e) {
            System.err.println("无法读取 config.properties: " + e.getMessage());
            return null;
        }
    }

    public static int compareVersion(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) return num1 - num2;
        }
        return 0;
    }


    public static void checkAndUpdate(Stage stage) {
        new Thread(() -> {
            try {
                System.out.println("正在检查更新...");
                System.setProperty("https.protocols", "TLSv1.2");
                String jsonStr = fetch(VERSION_JSON_URL);
                JSONObject json = new JSONObject(jsonStr);

                String latest = json.getString("latestVersion");
                String downloadUrl = json.getString("downloadUrl");
                String changelog = json.getString("changelog");
                String committer = json.getString("committer");
                String commitDate = json.getString("commitDate");

                int cmp = compareVersion(LOCAL_VERSION, latest);

                if (cmp < 0) {
                    System.out.println("发现新版本: " + latest);

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("发现新版本");
                        alert.setHeaderText("当前版本：" + LOCAL_VERSION + " → " + latest);

                        String content = "更新日志：" + changelog + "\n\n" +
                                "提交者：" + committer + "\n" +
                                "提交日期：" + commitDate + "\n" +
                                "是否立即下载并安装更新？";


                        alert.setContentText(content);


                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                stage.hide(); // 隐藏主窗口
                                File installer = downloadInstaller(downloadUrl);
                                if (installer != null) {
                                    Desktop.getDesktop().open(installer);
                                    Platform.exit();
                                    System.exit(0);
                                }
                            } catch (Exception ex) {
                                showError("更新失败", "下载或安装新版本时出错：" + ex.getMessage());
                            }
                        }
                    });

                } else {
                    System.out.println("已是最新版本：" + LOCAL_VERSION);
                }
            } catch (Exception e) {
                System.err.println("检查更新失败：" + e.getMessage());
            }
        }).start();
    }

    private static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    private static String fetch(String urlStr) throws IOException {
        URI uri = URI.create(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return in.lines().reduce("", (a, b) -> a + b + "\n");
        }
    }

    private static File downloadInstaller(String urlStr) throws IOException {
        URI uri = URI.create(urlStr);
        URL url = uri.toURL();
        Path tempFile = Files.createTempFile("CampusSystem-Update-", ".exe");
        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toFile();
    }

    private static String getLocalVersion() {
        try (InputStream in = AutoUpdater.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("app.version", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
