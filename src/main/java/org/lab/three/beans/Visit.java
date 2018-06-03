package org.lab.three.beans;

public class Visit {
    private int objectId;
    private int lessonId;
    private String date;
    private String mark;

    public Visit(int objectId, int lessonId, String date, String mark) {
        this.objectId = objectId;
        this.lessonId = lessonId;
        this.date = date;
        this.mark = mark;
    }

    public Visit() {
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
