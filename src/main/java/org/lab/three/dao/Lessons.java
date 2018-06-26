package org.lab.three.dao;

import java.util.Map;

public interface Lessons {
    void deleteAllLessons(int objectId);
    void updateLessons(int objectId, String value);
    Map<Integer, String> getAllObjectTypes();

}
