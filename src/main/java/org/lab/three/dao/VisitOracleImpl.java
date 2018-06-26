package org.lab.three.dao;

import org.apache.log4j.Logger;
import org.lab.three.beans.LWVisit;
import org.lab.three.connect.OracleConnect;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitOracleImpl implements Visit {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);
    private OracleConnect oracleConnect;

    /**
     * inserts data into lw_visit table
     *
     * @param lessonId
     * @param objectId
     * @param date
     * @param value
     */
    @Override
    public void insertVisit(String lessonId, String objectId, String date, String value) {
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("MERGE INTO lw_visit d\n" +
                    "USING (SELECT ? AS object_id, ? AS lesson_id, ? AS editDate, ? AS mark FROM dual) s\n" +
                    "ON (d.object_id = s.object_id AND d.lesson_id = s.lesson_id AND d.editDate = s.editDate)\n" +
                    "WHEN MATCHED THEN UPDATE SET d.mark = s.mark\n" +
                    "WHEN NOT MATCHED THEN INSERT VALUES (s.object_id,s.lesson_id, s.editDate, s.mark)");
            preparedStatement.setInt(1, Integer.parseInt(objectId));
            preparedStatement.setInt(2, Integer.parseInt(lessonId));
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, value);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception with merge into visit", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
    }

    /**
     * returns list of visits by lesson id
     *
     * @param lessonId
     * @return list
     */
    @Override
    public List<LWVisit> getVisitByLessonId(int lessonId) {
        List<LWVisit> list = new ArrayList<>();
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT OBJECT_ID,LESSON_ID,EDITDATE,MARK FROM lw_visit WHERE lesson_id = ?");
            preparedStatement.setInt(1, lessonId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(new LWVisit(resultSet.getInt(DAOOracleImpl.OBJECT_ID),
                        resultSet.getInt("lesson_id"),
                        resultSet.getString("editDate"),
                        resultSet.getString("mark")));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select from visit", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns objects id and name map by object type
     *
     * @param objectType
     * @return map
     */
    @Override
    public Map<Integer, String> getObjectsByObjectType(int objectType) {
        Map<Integer, String> objects = new HashMap<>();
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT object_id, name FROM LW_OBJECTS WHERE OBJECT_TYPE_ID = ?");
            preparedStatement.setInt(1, objectType);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                objects.put(resultSet.getInt(DAOOracleImpl.OBJECT_ID), resultSet.getString(DAOOracleImpl.NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get objects by object type id ", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return objects;
    }

    /**
     * returns students id and name map for particular lesson
     *
     * @param lessonId
     * @return map
     */
    @Override
    public Map<Integer, String> getStudentsByLessonId(int lessonId) {
        Map<Integer, String> map = new HashMap<>();
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.object_id, o.name " +
                    "FROM lw_params p, lw_objects o " +
                    "WHERE p.attr_id = 9 AND p.value = ? AND p.object_id = o.object_id");
            preparedStatement.setString(1, String.valueOf(lessonId));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(DAOOracleImpl.OBJECT_ID),
                        resultSet.getString(DAOOracleImpl.NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get students by lesson id ", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return map;
    }

    /**
     * returns list of unique dates by lesson id
     *
     * @param lessonId
     * @return list
     */
    @Override
    public List<String> getDistinctDateByLessonId(int lessonId) {
        connection = oracleConnect.connect();
        List<String> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT DISTINCT editDate FROM lw_visit WHERE LESSON_ID=? ORDER BY to_date(editDate,'dd.mm.yyyy')");
            preparedStatement.setInt(1, lessonId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString("editDate"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select from visit", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    public void setOracleConnect(OracleConnect oracleConnect) {
        this.oracleConnect = oracleConnect;
    }
}
