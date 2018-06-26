package org.lab.three.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface DAO {
    int createObject(String name, String parentId, String objectType);
    Map<Integer, String> getObjectTypes(int parentId);
    void executeScript(Connection connection);
    List<Integer> getAttrByObjectIdFromParams(int objectId);
    void updateParams(int objectId, int attrID, String value);
    Map<Integer, String> getAttrByObjectIdFromAOT(int objectType);
    String getRightByUserName(String name);
    String getNameById(int objectId);
    Map<Integer,String> getPath(int parentID);
    int getNextId();
}
