package com.work.javafx.model;

public  class CourseForScoreInput {
    private String id;
    private String name;
    private int peopleNum;
    private double regularRatio;
    private double finalRatio;

    public CourseForScoreInput(String id, String name, int peopleNum, double regularRatio, double finalRatio) {
        this.id = id;
        this.peopleNum = peopleNum;
        this.name = name;
        this.regularRatio = regularRatio;
        this.finalRatio = finalRatio;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRegularRatio() {
        return regularRatio;
    }

    public double getFinalRatio() {
        return finalRatio;
    }

    @Override
    public String toString() {
        return name;
    }
}