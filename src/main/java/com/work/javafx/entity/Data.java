package com.work.javafx.entity;

import javafx.collections.ObservableList;

public  class Data {
    private static Data instance;
    private String currentTerm;
    private ObservableList<String> semesterList; // 学期列表数据


    public static synchronized Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }
    //清除数据
    public void clearSession() {
        semesterList = null;
    }

    public Data() {
    }

    public Data(ObservableList<String> semesterList) {
        this.semesterList = semesterList;
    }

    public String getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(String currentTerm) {
        this.currentTerm = currentTerm;
    }

    public ObservableList<String> getSemesterList() {
        return semesterList;
    }
    public void setSemesterList(ObservableList<String> semesterList) {
        this.semesterList = semesterList;
    }
}
