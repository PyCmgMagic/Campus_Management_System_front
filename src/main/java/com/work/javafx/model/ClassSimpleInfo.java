package com.work.javafx.model;
public  class ClassSimpleInfo {
    String major; String number; int id;
    public ClassSimpleInfo() {}
    public ClassSimpleInfo(String m, String n, int i) { major=m; number=n; id=i; }
    public String getName(){ return major+number; } public String getMajor() { return major; }
    @Override public String toString() { return major+number; }
    public void setMajor(String m){major=m;} public String getNumber(){return number;}
    public void setNumber(String n){number=n;} public int getId(){return id;} public void setId(int i){id=i;}
}