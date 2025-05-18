package com.work.javafx.model;

/**
 * 成绩记录数据模型
 */
public class ScoreRecord {
    private final int index;
    private final String id;
    private final String courseName;
    private final double point;
    private final String type;
    private final String teacher;
    private final int grade;
    private final double gpa;
    private final String rank;
    private final int regular; // 平时成绩
    private final int finalScore;   // 期末成绩
    
    public ScoreRecord(int index, String id, String courseName, double point,
                      String type, String teacher, int grade, double gpa, String rank) {
        this.index = index;
        this.id = id;
        this.courseName = courseName;
        this.point = point;
        this.type = type;
        this.teacher = teacher;
        this.grade = grade;
        this.gpa = gpa;
        this.rank = rank;
        this.regular = 0;
        this.finalScore = 0;
    }
    
    public ScoreRecord(int index, String id, String courseName, double point,
                      String type, String teacher, int grade, double gpa, String rank,
                      int regular, int finalScore) {
        this.index = index;
        this.id = id;
        this.courseName = courseName;
        this.point = point;
        this.type = type;
        this.teacher = teacher;
        this.grade = grade;
        this.gpa = gpa;
        this.rank = rank;
        this.regular = regular;
        this.finalScore = finalScore;
    }
    
    // Getters
    public int getIndex() { return index; }
    public String getId() { return id; }
    public String getCourseName() { return courseName; }
    public double getPoint() { return point; }
    public String getType() { return type; }
    public String getTeacher() { return teacher; }
    public int getGrade() { return grade; }
    public double getGpa() { return gpa; }
    public String getRank() { return rank; }
    public int getRegular() { return regular; }
    public int getFinalScore() { return finalScore; }
} 