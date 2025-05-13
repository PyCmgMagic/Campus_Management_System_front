package com.work.javafx.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

// 学生数据模型类
public  class Student {
    private final SimpleStringProperty id;
    private final SimpleStringProperty sduid;
    private final SimpleStringProperty name;
    private final SimpleStringProperty gender;
    private final SimpleStringProperty department;
    private final SimpleStringProperty major;
    private final SimpleStringProperty grade;
    private final SimpleStringProperty className;
    private final SimpleStringProperty status;
    private final SimpleBooleanProperty selected;

    public Student(String id, String sduid,String name, String gender, String department,
                   String major, String grade, String className, String status) {
        this.id = new SimpleStringProperty(id);
        this.sduid = new SimpleStringProperty(sduid);
        this.name = new SimpleStringProperty(name);
        this.gender = new SimpleStringProperty(gender);
        this.department = new SimpleStringProperty(department);
        this.major = new SimpleStringProperty(major);
        this.grade = new SimpleStringProperty(grade);
        this.className = new SimpleStringProperty(className);
        this.status = new SimpleStringProperty(status);
        this.selected = new SimpleBooleanProperty(false);
    }

    // Getters and setters
    public String getId() { return id.get(); }
    public SimpleStringProperty idProperty() { return id; }
    public void setId(String id) { this.id.set(id); }
    public String getSduid() { return id.get(); }
    public SimpleStringProperty sduidProperty() { return sduid; }
    public void setSduid(String id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getGender() { return gender.get(); }
    public SimpleStringProperty genderProperty() { return gender; }
    public void setGender(String gender) { this.gender.set(gender); }

    public String getDepartment() { return department.get(); }
    public SimpleStringProperty departmentProperty() { return department; }
    public void setDepartment(String department) { this.department.set(department); }

    public String getMajor() { return major.get(); }
    public SimpleStringProperty majorProperty() { return major; }
    public void setMajor(String major) { this.major.set(major); }

    public String getGrade() { return grade.get(); }
    public SimpleStringProperty gradeProperty() { return grade; }
    public void setGrade(String grade) { this.grade.set(grade); }

    public String getClassName() { return className.get(); }
    public SimpleStringProperty classNameProperty() { return className; }
    public void setClassName(String className) { this.className.set(className); }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public boolean isSelected() { return selected.get(); }
    public SimpleBooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}