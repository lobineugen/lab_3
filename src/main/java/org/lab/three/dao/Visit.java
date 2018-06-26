package org.lab.three.dao;


import org.lab.three.beans.LWVisit;

import java.util.List;
import java.util.Map;

public interface Visit {
    void insertVisit(String lessonId, String objectId, String date, String value);
    List<LWVisit> getVisitByLessonId(int lessonId);
    List<String> getDistinctDateByLessonId(int lessonId);
    Map<Integer,String> getStudentsByLessonId(int lessonId);
    Map<Integer,String> getObjectsByObjectType(int objectType);

}
