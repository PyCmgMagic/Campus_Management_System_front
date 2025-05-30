package com.work.javafx.entity;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String refreshToken;
    private String username;
    private Integer identity;
    private Integer id;
    private String email;
    private String phone;
    private String sex;
    private String section;
    private String nation;
    private String ethnic;
    private String sduid;
    private String major;
    private String politicsStatus;
    private String getEmail;
    private String getPhone;
    private String college;
    private String admission;
    private String graduation;
    private String number;

    public String getNumber(){return  number; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void  setNumber(String number) { this.number = number; }

    private String induction;

    public String getInduction() {
        return induction;
    }

    public void setInduction(String induction) {
        this.induction = induction;
    }

    public String getAdmission(){
        return admission;
    }

    public void setAdmission(String admission){
        this.admission = admission;
    }

    public String getGraduation(){
        return graduation;
    }

    public void setGraduation(String graduation){
        this.graduation = graduation;
    }
    
    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
    
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

    public String getGetEmail() {
        return getEmail;
    }

    public void setGetEmail(String getEmail) {
        this.getEmail = getEmail;
    }

    public String getGetPhone() {
        return getPhone;
    }

    public void setGetPhone(String getPhone) {
        this.getPhone = getPhone;
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