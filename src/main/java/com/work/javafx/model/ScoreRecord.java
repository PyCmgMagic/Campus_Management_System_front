package com.work.javafx.model;

/**
 * 成绩记录数据模型
 */
public class ScoreRecord {
    private final int index;
    private final String courseCode;
    private final String courseName;
    private final double credit;
    private final String courseType;
    private final String teacher;
    private final int score;
    private final double gpa;
    private final String rank;
    private final int regularScore; // 平时成绩
    private final int finalScore;   // 期末成绩
    
    public ScoreRecord(int index, String courseCode, String courseName, double credit, 
                      String courseType, String teacher, int score, double gpa, String rank) {
        this.index = index;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.courseType = courseType;
        this.teacher = teacher;
        this.score = score;
        this.gpa = gpa;
        this.rank = rank;
        this.regularScore = 0;
        this.finalScore = 0;
    }
    
    public ScoreRecord(int index, String courseCode, String courseName, double credit, 
                      String courseType, String teacher, int score, double gpa, String rank,
                      int regularScore, int finalScore) {
        this.index = index;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.courseType = courseType;
        this.teacher = teacher;
        this.score = score;
        this.gpa = gpa;
        this.rank = rank;
        this.regularScore = regularScore;
        this.finalScore = finalScore;
    }
    
    // Getters
    public int getIndex() { return index; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public double getCredit() { return credit; }
    public String getCourseType() { return courseType; }
    public String getTeacher() { return teacher; }
    public int getScore() { return score; }
    public double getGpa() { return gpa; }
    public String getRank() { return rank; }
    public int getRegularScore() { return regularScore; }
    public int getFinalScore() { return finalScore; }
} 