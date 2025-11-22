package edu.ntu.ccds.sc2002.model;

public class Student extends User {
    private int year;         // 1..4
    private String major;     // e.g., CSC, EEE, MAE

    public Student(String id, String name, String password, int year, String major) {
        super(id, name, password, Role.STUDENT);
        this.year = year;
        this.major = major;
    }
    public int getYear(){ return year; }
    public String getMajor(){ return major; }

    @Override public String toString(){
        return super.toString() + " [Y" + year + " " + major + "]";
    }
}
