package com.work.javafx.util;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends Task<File> {

    private final String downloadUrl;
    private final File outputFile;

    public DownloadTask(String downloadUrl, File outputFile) {
        this.downloadUrl = downloadUrl;
        this.outputFile = outputFile;
    }

    @Override
    protected File call() throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int contentLength = conn.getContentLength();

        if (contentLength <= 0) {
            throw new IOException("无法获取文件大小");
        }

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                if (isCancelled()) {
                    updateMessage("下载已取消");
                    return null;
                }

                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                updateProgress(totalRead, contentLength);
            }

            updateMessage("下载完成");
            return outputFile;
        }
    }
}
