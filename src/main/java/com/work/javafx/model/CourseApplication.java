package com.work.javafx.model;

public class CourseApplication {
    private int id;
    private String teacherName;
    private String name;
    private String category;
    private int point;
    private int teacherId;
    private String classroom;
    private int weekStart;
    private int weekEnd;
    private int period;
    private Object time;
    private String college;
    private String term;
    private String classNum;
    private String type;
    private int capacity;
    private String status;
    private String intro;
    private int examination;
    private String f_reason;
    private boolean published;
    private double regularRatio;
    private double finalRatio;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public int getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(int weekStart) {
        this.weekStart = weekStart;
    }

    public int getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(int weekEnd) {
        this.weekEnd = weekEnd;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getClassNum() {
        return classNum;
    }

    public void setClassNum(String classNum) {
        this.classNum = classNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getExamination() {
        return examination;
    }

    public void setExamination(int examination) {
        this.examination = examination;
    }

    public String getF_reason() {
        return f_reason;
    }

    public void setF_reason(String f_reason) {
        this.f_reason = f_reason;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public double getRegularRatio() {
        return regularRatio;
    }

    public void setRegularRatio(double regularRatio) {
        this.regularRatio = regularRatio;
    }

    public double getFinalRatio() {
        return finalRatio;
    }

    public void setFinalRatio(double finalRatio) {
        this.finalRatio = finalRatio;
    }

    // 提供一个匹配 CourseManagementController 中使用的构造函数
     public CourseApplication( String courseName, String department,
                               int credit, String courseType) {
         this.teacherName = courseName;
         this.college = department;
         this.point = credit;
         this.type = courseType;

     }
}
