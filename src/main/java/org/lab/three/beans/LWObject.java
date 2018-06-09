package org.lab.three.beans;

import com.google.common.collect.Multimap;

/**
 * LWObject class is responsible for object entity
 */
public class LWObject {
    private int objectID;
    private int parentID;
    private int objectTypeID;
    private String name;
    private Multimap<String,String> params;


    LWObject() {

    }

    public LWObject(int objectID, int parentID, int objectTypeID, String name, Multimap<String,String> params) {
        this.objectID = objectID;
        this.parentID = parentID;
        this.objectTypeID = objectTypeID;
        this.name = name;
        this.params = params;
    }

    /**
     * @return object ID
     */
    public int getObjectID() {
        return objectID;
    }

    /**
     * Sets object ID
     *
     * @param objectID
     */
    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    /**
     * @return parent ID
     */
    public int getParentID() {
        return parentID;
    }

    /**
     * Sets parent object ID
     *
     * @param parentID
     */
    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    /**
     * @return object type ID
     */
    public int getObjectTypeID() {
        return objectTypeID;
    }

    /**
     * Sets object type id
     *
     * @param objectTypeID
     */
    public void setObjectTypeID(int objectTypeID) {
        this.objectTypeID = objectTypeID;
    }

    /**
     * @return object name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets object name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return collection of all parameters
     */
    public Multimap<String,String> getParams() {
        return params;
    }

    /**
     * Sets collection of all parameters
     *
     * @param params
     */
    public void setParams(Multimap<String,String> params) {
        this.params = params;
    }
}
