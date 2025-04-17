package com.work.javafx.model;

import javafx.beans.property.*;

public class CourseApplication {
    private final StringProperty courseCode = new SimpleStringProperty();
    private final StringProperty courseName = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final StringProperty applicantName = new SimpleStringProperty();
    private final StringProperty applicationDate = new SimpleStringProperty();
    private final IntegerProperty credit = new SimpleIntegerProperty();
    private final StringProperty courseType = new SimpleStringProperty();

    public CourseApplication(String courseCode, String courseName, String department, 
                            String applicantName, String applicationDate, 
                            Integer credit, String courseType) {
        this.courseCode.set(courseCode);
        this.courseName.set(courseName);
        this.department.set(department);
        this.applicantName.set(applicantName);
        this.applicationDate.set(applicationDate);
        this.credit.set(credit);
        this.courseType.set(courseType);
    }

    // CourseCode property
    public StringProperty courseCodeProperty() {
        return courseCode;
    }
    
    public String getCourseCode() {
        return courseCode.get();
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode.set(courseCode);
    }

    // CourseName property
    public StringProperty courseNameProperty() {
        return courseName;
    }
    
    public String getCourseName() {
        return courseName.get();
    }
    
    public void setCourseName(String courseName) {
        this.courseName.set(courseName);
    }

    // Department property
    public StringProperty departmentProperty() {
        return department;
    }
    
    public String getDepartment() {
        return department.get();
    }
    
    public void setDepartment(String department) {
        this.department.set(department);
    }

    // ApplicantName property
    public StringProperty applicantNameProperty() {
        return applicantName;
    }
    
    public String getApplicantName() {
        return applicantName.get();
    }
    
    public void setApplicantName(String applicantName) {
        this.applicantName.set(applicantName);
    }

    // ApplicationDate property
    public StringProperty applicationDateProperty() {
        return applicationDate;
    }
    
    public String getApplicationDate() {
        return applicationDate.get();
    }
    
    public void setApplicationDate(String applicationDate) {
        this.applicationDate.set(applicationDate);
    }

    // Credit property
    public IntegerProperty creditProperty() {
        return credit;
    }
    
    public Integer getCredit() {
        return credit.get();
    }
    
    public void setCredit(Integer credit) {
        this.credit.set(credit);
    }

    // CourseType property
    public StringProperty courseTypeProperty() {
        return courseType;
    }
    
    public String getCourseType() {
        return courseType.get();
    }
    
    public void setCourseType(String courseType) {
        this.courseType.set(courseType);
    }
} 