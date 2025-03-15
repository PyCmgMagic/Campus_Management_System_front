package com.work.javafx.entity;

public class Student {
    //用户唯一id
    private Integer id ;
    //用户名
    private String username;
    //邮箱
    private String email;
    //手机号
    private String phoneNumber;
    //学号
    private String student_id;

    public Student(){}

    public Student(Integer id, String username, String email, String phoneNumber, String student_id, String major, String permission, String nation, String ethnic) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.student_id = student_id;
        this.major = major;
        this.permission = permission;
        this.nation = nation;
        this.ethnic = ethnic;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getEthnic() {
        return ethnic;
    }

    public void setEthnic(String ethnic) {
        this.ethnic = ethnic;
    }

    //专业
    private String major;
    //权限
    private String permission;
    //国籍
    private String nation;
    //民族
    private String ethnic;
}
