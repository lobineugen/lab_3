package org.lab.three.beans;

import java.util.Map;

public class LWObject {
    private int objectID;
    private int parentID;
    private int objectTypeID;
    private String name;
    private Map<String, String> params;


    LWObject() {

    }

    public LWObject(int objectID, int parentID, int objectTypeID, String name, Map<String, String> params) {
        this.objectID = objectID;
        this.parentID = parentID;
        this.objectTypeID = objectTypeID;
        this.name = name;
        this.params = params;
    }

    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int object_id) {
        this.objectID = object_id;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parent_id) {
        this.parentID = parent_id;
    }

    public int getObjectTypeID() {
        return objectTypeID;
    }

    public void setObjectTypeID(int object_type_id) {
        this.objectTypeID = object_type_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
