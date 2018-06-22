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

    @Override
    public String toString() {
        return "LWObject{" +
                "objectID=" + objectID +
                ", parentID=" + parentID +
                ", objectTypeID=" + objectTypeID +
                ", name='" + name + '\'' +
                ", params=" + params +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LWObject lwObject = (LWObject) o;

        if (objectID != lwObject.objectID) return false;
        if (parentID != lwObject.parentID) return false;
        if (objectTypeID != lwObject.objectTypeID) return false;
        if (!name.equals(lwObject.name)) return false;
        return params.equals(lwObject.params);
    }

    @Override
    public int hashCode() {
        int result = objectID;
        result = 31 * result + parentID;
        result = 31 * result + objectTypeID;
        result = 31 * result + name.hashCode();
        result = 31 * result + params.hashCode();
        return result;
    }
}
