package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import java.util.List;
import java.util.Map;

public interface DAO {
    void connect();
    void disconnect();
    List<lwObject> getTopObject();
    List<lwObject> getChildren(int object_id);
    List<lwObject> removeByID(int[] object_id, int parent_id);
    void createObject(String name, String parentId, String objectType);
    Map<Integer,String> getObjectTypes(int parentId);

}
