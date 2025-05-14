package com.work.javafx.model;


/**
 * 课表行数据模型
 */
public  class CourseRow {
    private  String time = "";
    private  String monday = "";
    private  String tuesday = "";
    private  String wednesday = "";
    private  String thursday = "";
    private  String friday = "";
    private  String saturday = "";
    private  String sunday = "";

    public CourseRow(String time, String monday, String tuesday, String wednesday,
                     String thursday, String friday, String saturday, String sunday) {
        this.time = time;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public CourseRow() {
    }

    // Getters
    public String getTime() { return time; }
    public String getMonday() { return monday; }
    public String getTuesday() { return tuesday; }
    public String getWednesday() { return wednesday; }
    public String getThursday() { return thursday; }
    public String getFriday() { return friday; }
    public String getSaturday() { return saturday; }
    public String getSunday() { return sunday; }
    //Setter

    public void setTime(String time) {
        this.time = time;
    }

    public void setMonday(String monday) {
        this.monday = monday;
    }

    public void setTuesday(String tuesday) {
        this.tuesday = tuesday;
    }

    public void setWednesday(String wednesday) {
        this.wednesday = wednesday;
    }

    public void setThursday(String thursday) {
        this.thursday = thursday;
    }

    public void setFriday(String friday) {
        this.friday = friday;
    }

    public void setSaturday(String saturday) {
        this.saturday = saturday;
    }

    public void setSunday(String sunday) {
        this.sunday = sunday;
    }
}