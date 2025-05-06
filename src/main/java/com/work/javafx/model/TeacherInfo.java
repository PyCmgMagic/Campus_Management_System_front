package com.work.javafx.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * 教师数据模型的内部类。
 */
public  class TeacherInfo {
    private final SimpleStringProperty id;
    private final SimpleStringProperty sduid;
    private final SimpleStringProperty name;        // 映射自 username
    private final SimpleStringProperty college;
    private final SimpleStringProperty contactInfo; // 映射自 email
    private final SimpleStringProperty status;

    public String getSduid() {
        return sduid.get();
    }

    public SimpleStringProperty sduidProperty() {
        return sduid;
    }

    public TeacherInfo(String id, String sduid, String name, String college, String contactInfo, String status) {
        this.id = new SimpleStringProperty(id);
        this.sduid = new SimpleStringProperty(sduid);
        this.name = new SimpleStringProperty(name);
        this.college = new SimpleStringProperty(college);
        this.contactInfo = new SimpleStringProperty(contactInfo);
        this.status = new SimpleStringProperty(status);
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    // PropertyValueFactory 需要的 Getters
    public String getId() { return id.get(); } // 返回 sduid
    public SimpleStringProperty idProperty() { return id; }
    public String getName() { return name.get(); } // 返回 username
    public SimpleStringProperty nameProperty() { return name; }
    public String getCollege() { return college.get(); }
    public SimpleStringProperty collegeProperty() { return college; }
    public String getContactInfo() { return contactInfo.get(); } // Returns email
    public SimpleStringProperty contactInfoProperty() { return contactInfo; }
}