package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import java.util.List;

public interface DAO {
    void connect();
    void disconnect();
    List<lwObject> getTopObject();
    List<lwObject> getChildren(int object_id);
    List<lwObject> removeByID(int[] object_id, int parent_id);
}
