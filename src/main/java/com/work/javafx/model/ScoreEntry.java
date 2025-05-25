package com.work.javafx.model;

import com.work.javafx.controller.teacher.ScoreInputController;
import javafx.application.Platform;

public  class ScoreEntry {
    private final String studentId;
    private final String sduid;
    private final String name;
    private final String className;
    private final String courseName;
    private javafx.beans.property.IntegerProperty regularScore = new javafx.beans.property.SimpleIntegerProperty(0);
    private javafx.beans.property.IntegerProperty finalScore = new javafx.beans.property.SimpleIntegerProperty(0);
    private javafx.beans.property.IntegerProperty totalScore = new javafx.beans.property.SimpleIntegerProperty(0);
    private javafx.beans.property.StringProperty status = new javafx.beans.property.SimpleStringProperty("-");
    private String remarks;
    private double regularRatio = 0.3; // 默认平时成绩比例
    private double finalRatio = 0.7;   // 默认期末成绩比例
    private ScoreInputController controller; // 引用控制器以更新统计

    public ScoreEntry(String studentId, String sduid, String name, String className, String courseName, Integer regularScoreVal, Integer finalScoreVal, String remarks) {
        this.studentId = studentId;
        this.sduid = sduid;
        this.name = name;
        this.className = className;
        this.courseName = courseName;
        if (regularScoreVal != null) this.regularScore.set(regularScoreVal);
        if (finalScoreVal != null) this.finalScore.set(finalScoreVal);
        this.remarks = remarks;

        // 监听平时分和期末分的变化，自动更新总分和状态
        this.regularScore.addListener((obs, oldVal, newVal) -> {
            calculateTotalScore();
            updateStatus();
            // 通知控制器更新统计信息
            Platform.runLater(() -> {
                if (controller != null) {
                    controller.updateStatistics();
                }
            });
        });

        this.finalScore.addListener((obs, oldVal, newVal) -> {
            calculateTotalScore();
            updateStatus();
            // 通知控制器更新统计信息
            Platform.runLater(() -> {
                if (controller != null) {
                    controller.updateStatistics();
                }
            });
        });

        calculateTotalScore();
        updateStatus();
    }

    // 设置控制器引用
    public void setController(ScoreInputController controller) {
        this.controller = controller;
    }

    // 设置成绩比例
    public void setScoreRatios(double regularRatio, double finalRatio) { // Changed parameters to double
        this.regularRatio = regularRatio;
        this.finalRatio = finalRatio;
        calculateTotalScore();
        updateStatus();
    }

    // --- Getters ---
    public String getStudentId() {
        return studentId;
    }

    public String getSduid() {
        return sduid;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getCourseName() {
        return courseName;
    }

    // JavaFX 属性方法
    public Integer getRegularScore() {
        return regularScore.get();
    }

    public javafx.beans.property.IntegerProperty regularScoreProperty() {
        return regularScore;
    }

    public void setRegularScore(Integer value) {
        this.regularScore.set(value != null ? value : 0);
    }

    public Integer getFinalScore() {
        return finalScore.get();
    }

    public javafx.beans.property.IntegerProperty finalScoreProperty() {
        return finalScore;
    }

    public void setFinalScore(Integer value) {
        this.finalScore.set(value != null ? value : 0);
    }

    public Integer getTotalScore() {
        return totalScore.get();
    }

    public javafx.beans.property.IntegerProperty totalScoreProperty() {
        return totalScore;
    }

    public String getStatus() {
        return status.get();
    }

    public javafx.beans.property.StringProperty statusProperty() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // --- 计算逻辑 ---
    private void calculateTotalScore() {
        double regScore = regularScore.get();
        double finScore = finalScore.get();
        // Calculate with double precision and then round to nearest integer
        int total = (int) Math.round(regScore * regularRatio + finScore * finalRatio);
        totalScore.set(total);
    }

    private void updateStatus() {
        double total = totalScore.get(); // Keep as double for comparison consistency
        if (total >= 60) {
            status.set("通过");
        } else {
            status.set("不及格");
        }
    }
}