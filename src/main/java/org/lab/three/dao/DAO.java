package org.lab.three.dao;

import com.google.common.collect.Multimap;
import org.lab.three.beans.LWObject;
import org.lab.three.beans.Visit;
import java.util.List;
import java.util.Map;

public interface DAO {
    void connect();
    void disconnect();
    List<LWObject> getTopObject();
    List<LWObject> getChildren(int objectID);
    List<LWObject> removeByID(int[] objectID, String parentID);
    int createObject(String name, String parentId, String objectType);
    Map<Integer, String> getObjectTypes(int parentId);
    LWObject getObjectById(int objectId);
    List<LWObject> changeNameById(int objectId, String name);
    int checkTables();
    void executeScript();
    Multimap<String,String> getParamsById(int objectId);
    List<Integer> getAttrByObjectIdFromParams(int objectId);
    void updateParams(int objectId, int attrID, String value);
    Map<Integer, String> getAttrByObjectIdFromAOT(int objectType);
    int getNextId();
    List<LWObject> getObjectsListByObject(int objectId);
    Map<Integer, String> getAllObjectTypes();
    List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID);
    Map<Integer,String> getObjectsByObjectType(int objectType);
    Map<Integer,String> getStudentsByLessonId(int lessonId);
    void insertVisit(String lessonId, String objectId, String date, String value);
    List<Visit> getVisitByLessonId(int lessonId);
    List<String> getDistinctDateByLessonId(int lessonId);
    String getRightByUserName(String name);
    String getNameById(int objectId);
    void deleteAllLessons(int objectId);
    void updateLessons(int objectId, String value);

}
