package com.work.javafx.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public  class ClassInfo {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty department;
    private final SimpleStringProperty grade;
    private final SimpleStringProperty counselor;
    private final SimpleIntegerProperty studentCount;
    private final SimpleStringProperty status;

    public ClassInfo(String id, String name, String department, String grade, String counselor, int studentCount, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.department = new SimpleStringProperty(department);
        this.grade = new SimpleStringProperty(grade);
        this.counselor = new SimpleStringProperty(counselor);
        this.studentCount = new SimpleIntegerProperty(studentCount);
        this.status = new SimpleStringProperty(status);
    }

    public String getId() { return id.get(); }
    public SimpleStringProperty idProperty() { return id; }
    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }
    public String getDepartment() { return department.get(); }
    public SimpleStringProperty departmentProperty() { return department; }
    public String getGrade() { return grade.get(); }
    public SimpleStringProperty gradeProperty() { return grade; }
    public String getCounselor() { return counselor.get(); }
    public SimpleStringProperty counselorProperty() { return counselor; }
    public int getStudentCount() { return studentCount.get(); }
    public SimpleIntegerProperty studentCountProperty() { return studentCount; }
    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
}