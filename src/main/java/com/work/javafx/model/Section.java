package com.work.javafx.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Section {
    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty className = new SimpleStringProperty();
    private final SimpleStringProperty major = new SimpleStringProperty();
    private final SimpleIntegerProperty grade = new SimpleIntegerProperty();
    private final SimpleIntegerProperty teacherId = new SimpleIntegerProperty();
    private final SimpleStringProperty number = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Section() {
    }

    public Section(int id, String className, String major, int grade, int teacherId, String number) {
        this.id.set(id);
        this.className.set(className);
        this.major.set(major);
        this.grade.set(grade);
        this.teacherId.set(teacherId);
        this.number.set(number);
    }

    // ID
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    // 班级名称
    public String getClassName() {
        return className.get();
    }

    public SimpleStringProperty classNameProperty() {
        return className;
    }

    public void setClassName(String className) {
        this.className.set(className);
    }

    // 专业
    public String getMajor() {
        return major.get();
    }

    public SimpleStringProperty majorProperty() {
        return major;
    }

    public void setMajor(String major) {
        this.major.set(major);
    }

    // 年级
    public int getGrade() {
        return grade.get();
    }

    public SimpleIntegerProperty gradeProperty() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade.set(grade);
    }

    // 辅导员ID
    public int getTeacherId() {
        return teacherId.get();
    }

    public SimpleIntegerProperty teacherIdProperty() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId.set(teacherId);
    }

    // 班级编号
    public String getNumber() {
        return number.get();
    }

    public SimpleStringProperty numberProperty() {
        return number;
    }

    public void setNumber(String number) {
        this.number.set(number);
    }

    // 是否选中
    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + getId() +
                ", className='" + getClassName() + '\'' +
                ", major='" + getMajor() + '\'' +
                ", grade=" + getGrade() +
                ", teacherId=" + getTeacherId() +
                ", number='" + getNumber() + '\'' +
                '}';
    }
} 