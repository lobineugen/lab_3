package org.lab.three.beans;

public class lwObject {
    private int object_id;
    private int parent_id;
    private int object_type_id;
    private String name;

    lwObject(){

    }

    public lwObject(int object_id, int parent_id, int object_type_id, String name) {
        this.object_id = object_id;
        this.parent_id = parent_id;
        this.object_type_id = object_type_id;
        this.name = name;
    }

    public int getObject_id() {
        return object_id;
    }

    public void setObject_id(int object_id) {
        this.object_id = object_id;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public int getObject_type_id() {
        return object_type_id;
    }

    public void setObject_type_id(int object_type_id) {
        this.object_type_id = object_type_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
