package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import java.util.List;

public interface DAO {
    void connect();
    void disconnect();
    List<lwObject> getObjects();
}
