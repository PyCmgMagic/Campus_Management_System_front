package com.work.javafx.model;

import javafx.beans.property.*;

public class Course {
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final IntegerProperty credit = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty teacher = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Course(String code, String name, String department, Integer credit, String type, String teacher, Boolean isActive) {
        this.code.set(code);
        this.name.set(name);
        this.department.set(department);
        this.credit.set(credit);
        this.type.set(type);
        this.teacher.set(teacher);
        this.isActive.set(isActive);
    }

    // Code property
    public StringProperty codeProperty() {
        return code;
    }
    
    public String getCode() {
        return code.get();
    }
    
    public void setCode(String code) {
        this.code.set(code);
    }

    // Name property
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
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

    // Type property
    public StringProperty typeProperty() {
        return type;
    }
    
    public String getType() {
        return type.get();
    }
    
    public void setType(String type) {
        this.type.set(type);
    }

    // Teacher property
    public StringProperty teacherProperty() {
        return teacher;
    }
    
    public String getTeacher() {
        return teacher.get();
    }
    
    public void setTeacher(String teacher) {
        this.teacher.set(teacher);
    }

    // IsActive property
    public BooleanProperty isActiveProperty() {
        return isActive;
    }
    
    public Boolean getIsActive() {
        return isActive.get();
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive.set(isActive);
    }

    // Selected property
    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    public Boolean getSelected() {
        return selected.get();
    }
    
    public void setSelected(Boolean selected) {
        this.selected.set(selected);
    }
} 