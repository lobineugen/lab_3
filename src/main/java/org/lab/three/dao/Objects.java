package org.lab.three.dao;

import org.lab.three.beans.LWObject;

import java.util.List;

public interface Objects {
    List<LWObject> getTopObject();
    List<LWObject> getChildren(int objectID);
    List<LWObject> removeByID(int[] objectID, String parentID);
    List<LWObject> changeNameById(int objectId, String name);
    List<LWObject> getObjectsListByObject(int objectId);
    List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID);
    List<LWObject> getParentByChildren(int objectID);
    LWObject getObjectById(int objectId);
}
