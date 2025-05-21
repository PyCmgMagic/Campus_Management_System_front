package com.work.javafx.model;


public  class StudentInfo {
    private int id;
    private String username;
    private String sduid;

    public int getId() {
        return id;
    }

    public String getSduid() {
        return sduid;
    }

    public void setSduid(String sduid) {
        this.sduid = sduid;
    }

    public String getUsername() {
        return username;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public StudentInfo(int id, String username, String sduid) {
        this.id = id;
        this.username = username;
        this.sduid = sduid;
    }

    public StudentInfo() {}
}
