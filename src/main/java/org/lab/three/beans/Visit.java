package org.lab.three.beans;

/**
 * Visit class is responsible for visit dates and evaluation on subjects
 */
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

    /**
     * @return object ID
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * Sets object ID
     *
     * @param objectId
     */
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**
     * @return lesson ID
     */
    public int getLessonId() {
        return lessonId;
    }

    /**
     * Sets lesson ID
     *
     * @param lessonId
     */
    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    /**
     * @return date of lesson conducting
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets date of lesson conducting
     *
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return evaluation on subject
     */
    public String getMark() {
        return mark;
    }

    /**
     * Sets evaluation on subject
     *
     * @param mark
     */
    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Visit visit = (Visit) o;

        if (objectId != visit.objectId) return false;
        if (lessonId != visit.lessonId) return false;
        if (!date.equals(visit.date)) return false;
        return mark.equals(visit.mark);
    }

    @Override
    public int hashCode() {
        int result = objectId;
        result = 31 * result + lessonId;
        result = 31 * result + date.hashCode();
        result = 31 * result + mark.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "objectId=" + objectId +
                ", lessonId=" + lessonId +
                ", date='" + date + '\'' +
                ", mark='" + mark + '\'' +
                '}';
    }
}
