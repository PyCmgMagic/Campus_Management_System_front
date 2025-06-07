package com.work.javafx.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.work.javafx.entity.UserSession;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * JavaFX网络请求工具类
 */
public class NetworkUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    private static final int TIMEOUT = 30000; // 超时时间，单位毫秒
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    public static String BaseUrl;
    String token = "Bearer " + UserSession.getInstance().getToken();
    static {
        loadConfiguration();
    }
    /**
     * 从配置文件加载服务器配置
     */
    private static void loadConfiguration() {
        try (InputStream in = ClassLoader.getSystemResourceAsStream("application.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                BaseUrl = props.getProperty("server.url");
                System.out.println("从 resources 中加载配置成功，BaseUrl = " + BaseUrl);
            } else {
                LOGGER.warning("application.properties 未包含在资源路径中");
                ShowMessage.showErrorMessage("无配置文件", "找不到内置配置，请根据README配置URL");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "加载配置失败", e);
            ShowMessage.showErrorMessage("配置错误", "加载配置失败：" + e.getMessage());
        }
    }
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
        void onSuccess(T result) throws IOException;
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
        String finalUrlString = BaseUrl + urlString;
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return sendRequest(finalUrlString, method, headers, body);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    callback.onSuccess(task.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
        String finalUrlString = BaseUrl + urlString;
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendRequest(finalUrlString, method, headers, body);
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
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();
        headers.put("Authorization", token);
        request(urlString, HttpMethod.GET, headers, null, callback);
    }
    
    /**
     * 发送HTTP GET请求(带参数)
     *
     * @param urlString 请求URL
     * @param params URL参数
     * @param callback 回调处理
     */
    public static void get(String urlString, Map<String, String> params, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();

        headers.put("Authorization", token);

        String fullUrl = appendQueryParams(urlString, params);
        request(fullUrl, HttpMethod.GET, headers, null, callback);
    }
    
    /**
     * 发送HTTP GET请求(带参数和请求头)
     *
     * @param urlString 请求URL
     * @param params URL参数
     * @param headers 请求头
     * @param callback 回调处理
     */
    public static void get(String urlString, Map<String, String> params, 
                          Map<String, String> headers, Callback<String> callback) {
        String fullUrl = appendQueryParams(urlString, params);
        request(fullUrl, HttpMethod.GET, headers, null, callback);
    }
    
    /**
     * 异步发送HTTP GET请求
     *
     * @param urlString 请求URL
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> getAsync(String urlString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();

        headers.put("Authorization", token);
        return requestAsync(urlString, HttpMethod.GET, headers, null);
    }
    
    /**
     * 异步发送HTTP GET请求(带参数)
     *
     * @param urlString 请求URL
     * @param params URL参数
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> getAsync(String urlString, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();

        headers.put("Authorization", token);

        String fullUrl = appendQueryParams(urlString, params);
        return requestAsync(fullUrl, HttpMethod.GET, headers, null);
    }

    /**
     * 发送HTTP POST请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @param callback 回调处理
     */
    public static void post(String urlString, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();
        headers.put("Authorization", token);
        request(urlString, HttpMethod.POST, headers, body, callback);
    }    /**
     * 发送HTTP POST请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @param callback 回调处理
     * @param params 参数
     */
    public static void post(String urlString,Map<String,String> params, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();
        headers.put("Authorization", token);
        String fullUrl = appendQueryParams(urlString, params);
        request(fullUrl, HttpMethod.POST, headers, body, callback);
    }
    
    /**
     * 发送HTTP POST请求(带请求头)
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param callback 回调处理
     */
    public static void post(String urlString, String body, Map<String, String> headers, Callback<String> callback) {
        request(urlString, HttpMethod.POST, headers, body, callback);
    }
    
    /**
     * 异步发送HTTP POST请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> postAsync(String urlString, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();
        headers.put("Authorization", token);

        return requestAsync(urlString, HttpMethod.POST, headers, body);
    }
        /**
     * 异步发送HTTP POST请求(带params)
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> postAsync(String urlString,Map<String,String> params, String body ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        String token = "Bearer " + UserSession.getInstance().getToken();
        headers.put("Authorization", token);
        String fullUrl = appendQueryParams(urlString, params);
        return requestAsync(fullUrl, HttpMethod.POST, headers, body);
    }

    /**
     * 发送HTTP PUT请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @param callback 回调处理
     */
    public static void put(String urlString, String body, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        request(urlString, HttpMethod.PUT, headers, body, callback);
    }
    
    /**
     * 异步发送HTTP PUT请求
     *
     * @param urlString 请求URL
     * @param body 请求体
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> putAsync(String urlString, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        return requestAsync(urlString, HttpMethod.PUT, headers, body);
    }
    
    /**
     * 发送HTTP DELETE请求
     *
     * @param urlString 请求URL
     * @param callback 回调处理
     */
    public static void delete(String urlString, Callback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        request(urlString, HttpMethod.DELETE, headers, null, callback);
    }
    
    /**
     * 异步发送HTTP DELETE请求
     *
     * @param urlString 请求URL
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> deleteAsync(String urlString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        return requestAsync(urlString, HttpMethod.DELETE, headers, null);
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
     * 附加URL查询参数
     *
     * @param urlString 基础URL
     * @param params 参数Map
     * @return 附加了参数的完整URL
     */
    private static String appendQueryParams(String urlString, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return urlString;
        }
        
        StringBuilder urlBuilder = new StringBuilder(urlString);
        if (!urlString.contains("?")) {
            urlBuilder.append("?");
        } else if (!urlString.endsWith("&") && !urlString.endsWith("?")) {
            urlBuilder.append("&");
        }
        
        String queryString = params.entrySet().stream()
            .map(entry -> {
                try {
                    return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
                         + URLEncoder.encode(entry.getValue(), "UTF-8");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "URL参数编码失败", e);
                    return entry.getKey() + "=" + entry.getValue();
                }
            })
            .collect(Collectors.joining("&"));
            
        urlBuilder.append(queryString);
        return urlBuilder.toString();
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
                if (!connection.getRequestProperties().containsKey("Content-Type")) {
                    connection.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
                }

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
     * 设置默认连接超时时间
     * 
     * @param timeoutMillis 超时时间(毫秒)
     */
    public static void setDefaultTimeout(int timeoutMillis) {
        // 不可直接修改TIMEOUT常量，此处仅作为示例
        // 实际应用中可以使用一个非final变量来支持超时设置
        LOGGER.info("设置超时时间: " + timeoutMillis + "ms");
    }
    
    /**
     * 关闭线程池，应用退出时调用
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
    }

    /**
     * 异步发送HTTP POST请求，用于上传文件 (multipart/form-data)
     *
     * @param urlString 请求URL (相对路径)
     * @param file 要上传的文件
     * @return CompletableFuture对象，包含响应结果
     */
    public static CompletableFuture<String> postMultipartFileAsync(String urlString, File file) {
        String finalUrlString = BaseUrl + urlString;
        String boundary = "===" + System.currentTimeMillis() + "==="; // 定义边界
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            DataOutputStream dos = null;
            FileInputStream fileInputStream = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(finalUrlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true); // 允许输入流
                connection.setDoOutput(true); // 允许输出流
                connection.setUseCaches(false); // 不允许使用缓存
                connection.setRequestMethod(HttpMethod.POST.name());
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                String token = "Bearer " + UserSession.getInstance().getToken();
                if (token != null && !token.isEmpty() && !token.equals("Bearer null")) {
                     connection.setRequestProperty("Authorization", token);
                }

                dos = new DataOutputStream(connection.getOutputStream());

                // 文件部分
                String filename = file.getName();
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + lineEnd);
                 // 根据文件类型设置 Content-Type，这里简单处理 Excel
                if (filename.endsWith(".xlsx")) {
                    dos.writeBytes("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" + lineEnd);
                } else if (filename.endsWith(".xls")) {
                     dos.writeBytes("Content-Type: application/vnd.ms-excel" + lineEnd);
                } else {
                     dos.writeBytes("Content-Type: application/octet-stream" + lineEnd); // 默认类型
                }
                dos.writeBytes(lineEnd);

                // 读取文件内容并写入输出流
                fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.writeBytes(lineEnd);

                // 结束标记
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                dos.flush();

                // 获取响应 (复用部分 sendRequest 中的逻辑)
                int responseCode = connection.getResponseCode();
                StringBuilder response = new StringBuilder();

                InputStream inputStream;
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                    // 如果连错误流都没有，则直接抛出基础错误
                    if (inputStream == null) {
                         throw new IOException("HTTP请求失败，状态码: " + responseCode);
                    }
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                } // try-with-resources 会自动关闭 BufferedReader 和底层的 InputStream

                // 在读取完响应体后再检查状态码决定是否抛出异常
                if (responseCode >= 400) {
                    throw new IOException("HTTP请求失败，状态码: " + responseCode + "，错误信息: " + response.toString());
                }

                return response.toString(); // Supplier 需要返回一个 String

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "文件上传异常: " + e.getMessage(), e);
                // 将受检异常包装成运行时异常，以便 CompletableFuture 处理
                 throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
            } finally {
                // 关闭资源
                 if (fileInputStream != null) {
                    try { fileInputStream.close(); } catch (IOException e) { LOGGER.log(Level.WARNING, "关闭文件输入流失败", e); }
                }
                // dos (DataOutputStream) 会在 try-with-resources 或 connection.getOutputStream() 关闭时自动处理，一般无需手动关闭外部流
                // reader 已经被 try-with-resources 处理
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, EXECUTOR); // 在线程池中执行
    }
}