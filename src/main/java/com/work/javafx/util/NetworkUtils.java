package com.work.javafx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * JavaFX网络请求工具类
 */
public class NetworkUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    private static final int TIMEOUT = 10000; // 超时时间，单位毫秒
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    /**
     * 方法枚举
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }
    /**
     * 网络请求结果回调接口
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    /**
     * 异步发送HTTP请求
     *
     * @param urlString 请求URL
     * @param method HTTP方法
     * @param headers 请求头
     * @param body 请求体，GET请求时可为null
     * @param callback 回调处理
     */
    public static void request(String urlString, HttpMethod method, Map<String, String> headers,
                               String body, Callback<String> callback) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return sendRequest(urlString, method, headers, body);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> callback.onSuccess(task.getValue()));
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> callback.onFailure((Exception) task.getException()));
        });

        EXECUTOR.submit(task);
    }

    /**
     * 使用CompletableFuture发送HTTP请求
     *
     * @param urlString 请求URL
     * @param method HTTP方法
     * @param headers 请求头
     * @param body 请求体，GET请求时可为null
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> requestAsync(String urlString, HttpMethod method,
                                                         Map<String, String> headers, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendRequest(urlString, method, headers, body);
            } catch (IOException e) {
                throw new RuntimeException("网络请求失败", e);
            }
        }, EXECUTOR);
    }

    /**
     * 发送HTTP GET请求
     *
     * @param urlString 请求URL
     * @param callback 回调处理
     */
    public static void get(String urlString, Callback<String> callback) {
       Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        request(urlString, HttpMethod.GET, headers, null, callback);
    }

    /**
     * 发送HTTP POST请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @param callback 回调处理
     */
    public static void post(String urlString, String body, Callback<String> callback) {
        request(urlString, HttpMethod.POST, null, body, callback);
    }

    /**
     * 执行UI线程上的操作
     *
     * @param action 要执行的操作
     */
    public static void runOnUiThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * 执行网络请求并在UI线程上处理结果
     *
     * @param urlString 请求URL
     * @param method HTTP方法
     * @param onSuccess 成功回调
     * @param onError 失败回调
     */
    public static void executeRequest(String urlString, HttpMethod method,
                                      Consumer<String> onSuccess, Consumer<Exception> onError) {
        requestAsync(urlString, method, null, null)
                .thenAccept(result -> runOnUiThread(() -> onSuccess.accept(result)))
                .exceptionally(ex -> {
                    runOnUiThread(() -> onError.accept((Exception) ex.getCause()));
                    return null;
                });
    }

    /**
     * 发送HTTP请求的核心方法
     *
     * @param urlString 请求URL
     * @param method HTTP方法
     * @param headers 请求头
     * @param body 请求体
     * @return 响应结果
     * @throws IOException 请求异常时抛出
     */
    private static String sendRequest(String urlString, HttpMethod method,
                                      Map<String, String> headers, String body) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 创建连接
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.name());
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setDoInput(true);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 处理请求体
            if (body != null && !body.isEmpty() &&
                    (method == HttpMethod.POST || method == HttpMethod.PUT)) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // 获取响应
            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                // 成功响应
                reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                // 错误响应
                if (connection.getErrorStream() != null) {
                    reader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                } else {
                    throw new IOException("HTTP请求失败，状态码: " + responseCode);
                }
            }

            // 读取响应内容
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            if (responseCode >= 400) {
                throw new IOException("HTTP请求失败，状态码: " + responseCode +
                        "，错误信息: " + response.toString());
            }

            return response.toString();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "网络请求异常: " + e.getMessage(), e);
            throw new IOException("网络请求失败: " + e.getMessage(), e);
        } finally {
            // 关闭资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "关闭读取器失败", e);
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 关闭线程池，应用退出时调用
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}