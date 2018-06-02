package org.lab.three.dao;

import org.lab.three.beans.LWObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DAO {
    void connect();
    void disconnect();
    List<LWObject> getTopObject();
    List<LWObject> getChildren(int object_id);
    List<LWObject> removeByID(int[] object_id, String parent_id);
    int createObject(String name, String parentId, String objectType);
    Map<Integer,String> getObjectTypes(int parentId);
    LWObject getObjectById(int objectId);
    List<LWObject> changeNameById(int objectId, String name);
    int checkTables();
    void executeScript();
    Map<String, String> getParamsById(int objectId);
    ArrayList<Integer> getAttrByObjectIdFromParams(int objectId);
    void updateParams(int objectId, int attr_id, String value);
    Map<Integer,String> getAttrByObjectIdFromAOT(int object_type);
    int getNextId();
    List<LWObject> getObjectsListByObject(int objectId);
    Map<Integer, String> getAllObjectTypes();
    List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID);
}
