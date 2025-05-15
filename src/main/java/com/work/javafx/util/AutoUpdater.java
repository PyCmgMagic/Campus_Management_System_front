package com.work.javafx.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

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

    public static void checkAndUpdate(Stage stage) {
        new Thread(() -> {
            try {
                System.out.println("正在检查更新...");
                System.setProperty("https.protocols", "TLSv1.2");
                String jsonStr = fetch(VERSION_JSON_URL);
                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

                String latest = json.get("latestVersion").getAsString();
                String downloadUrl = json.get("downloadUrl").getAsString();

                if (!LOCAL_VERSION.equals(latest)) {
                    System.out.println("发现新版本: " + latest);

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("发现新版本");
                        alert.setHeaderText("当前版本：" + LOCAL_VERSION + " → " + latest);
                        alert.setContentText("是否立即下载并安装更新？");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            stage.hide(); // 隐藏主窗口

                            File outputFile = new File(System.getProperty("java.io.tmpdir"), "CampusManageSystemInstaller.msi");
                            DownloadTask task = new DownloadTask(downloadUrl, outputFile);

                            ProgressBar progressBar = new ProgressBar();
                            progressBar.progressProperty().bind(task.progressProperty());

                            Label label = new Label("正在下载更新...");
                            label.textProperty().bind(task.messageProperty());

                            Label percentLabel = new Label("0%");

                            task.progressProperty().addListener((obs, oldVal, newVal) -> {
                                if (newVal != null) {
                                    double progress = newVal.doubleValue();
                                    if (progress >= 0 && progress <= 1) {
                                        int percent = (int) (progress * 100);
                                        percentLabel.setText(percent + "%");
                                    }
                                }
                            });

                            Button cancelBtn = new Button("取消");
                            cancelBtn.setOnAction(e -> task.cancel());

                            VBox vbox = new VBox(10, label, progressBar, percentLabel, cancelBtn);
                            vbox.setAlignment(Pos.CENTER);
                            vbox.setPadding(new Insets(20));

                            Stage progressStage = new Stage();
                            progressStage.setTitle("下载更新中");
                            progressStage.setScene(new Scene(vbox, 350, 180)); // 调整高度以容纳百分比
                            progressStage.show();

                            task.setOnSucceeded(e -> {
                                progressStage.close();
                                File file = task.getValue();
                                if (file != null && file.exists()) {
                                    try {
                                        Desktop.getDesktop().open(file);
                                        Platform.exit();
                                        System.exit(0);
                                    } catch (IOException ex) {
                                        showError("安装失败", "无法启动安装程序：" + ex.getMessage());
                                    }
                                }
                            });

                            task.setOnFailed(e -> {
                                progressStage.close();
                                showError("下载失败", "更新下载过程中发生错误：" + task.getException().getMessage());
                            });

                            task.setOnCancelled(e -> {
                                progressStage.close();
                                showError("取消下载", "您已取消了更新下载。");
                            });

                            new Thread(task).start();
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