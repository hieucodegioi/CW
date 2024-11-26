package com.example.adminapp;

public class Class {
    private long classId;
    private String className;
    private String teacher;
    private String date;
    private String comments;

    // Constructor
    public Class(long classId, String className, String teacher, String date, String comments) {
        this.classId = classId;
        this.className = className;
        this.teacher = teacher;
        this.date = date;
        this.comments = comments;
    }

    // Getter methods
    public long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getDate() {
        return date;
    }

    public String getComments() {
        return comments;
    }
}
