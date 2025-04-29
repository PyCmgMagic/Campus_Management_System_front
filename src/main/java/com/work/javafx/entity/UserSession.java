package com.work.javafx.entity;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String refreshToken;
    private String username;
    private Integer identity;
    private String email;
    private String phone;
    private String sex;
    private String section;
    private String nation;
    private String ethnic;
    private String sduid;
    private String major;
    private String politicsStatus;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
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

    public String getSduid() {
        return sduid;
    }

    public void setSduid(String sduid) {
        this.sduid = sduid;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPoliticsStatus() {
        return politicsStatus;
    }

    public void setPoliticsStatus(String politicsStatus) {
        this.politicsStatus = politicsStatus;
    }


    public Integer getIdentity() {
        return identity;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setIdentity(Integer identity) {
        this.identity = identity;
    }

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 设置/获取token的方法
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    // 设置/获取用户名的方法
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    // 清除会话
    public void clearSession() {
        token = null;
        username = null;
        identity = null;
    }
}
//示例
// 登录时保存token
//UserSession.getInstance().setToken("eyJhbGciOiJIUzI1NiIsInR5...");
//UserSession.getInstance().setUsername("user123");
//UserSession.getInstance().setIdentity("user123");

// 在应用其他部分获取token
//String token = UserSession.getInstance().getToken();

// 退出登录时清除
//UserSession.getInstance().clearSession();