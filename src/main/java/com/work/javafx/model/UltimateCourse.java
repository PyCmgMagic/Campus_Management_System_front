package com.work.javafx.model;


import javafx.scene.layout.HBox;

public class UltimateCourse {
    private int id;
    private String name;
    private String teacherName;
    private String category;
    private int point;
    private int teacherId;
    private String classroom;
    private int weekStart;
    private int weekEnd;
    private int period;
    private String time;
    private String college;
    
    // 添加表格绑定需要的属性
    private String courseCode;
    private String courseName;
    private String otherTeachers;
    private String semester;
    private String credits;
    private String studentCount;
    private String syllabusStatus;
    private transient HBox actions;

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

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
    public UltimateCourse() {
    }

    public UltimateCourse(int id, String name, String category, int point, int teacherId, String classroom,
                  int weekStart, int weekEnd, int period, String time, String college, String term,
                  String classNum, String type, int capacity, String status, String intro,
                  int examination, String f_reason, boolean published, double regularRatio, double finalRatio) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.point = point;
        this.teacherId = teacherId;
        this.classroom = classroom;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.period = period;
        this.time = time;
        this.college = college;
        this.term = term;
        this.classNum = classNum;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
        this.intro = intro;
        this.examination = examination;
        this.f_reason = f_reason;
        this.published = published;
        this.regularRatio = regularRatio;
        this.finalRatio = finalRatio;
    }

    // Getters and Setters
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getOtherTeachers() {
        return otherTeachers;
    }

    public void setOtherTeachers(String otherTeachers) {
        this.otherTeachers = otherTeachers;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(String studentCount) {
        this.studentCount = studentCount;
    }

    public String getSyllabusStatus() {
        return syllabusStatus;
    }

    public void setSyllabusStatus(String syllabusStatus) {
        this.syllabusStatus = syllabusStatus;
    }

    public HBox getActions() {
        return actions;
    }

    public void setActions(HBox actions) {
        this.actions = actions;
    }
}