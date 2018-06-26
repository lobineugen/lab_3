package org.lab.three.dao;

import org.apache.log4j.Logger;
import org.lab.three.connect.OracleConnect;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LessonsOracleImpl implements Lessons {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);
    private OracleConnect oracleConnect;

    /**
     * deletes lessons from lw_params table
     *
     * @param objectId
     */
    @Override
    public void deleteAllLessons(int objectId) {
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM lw_params WHERE object_id = ? AND attr_id = 9");
            preparedStatement.setInt(1, objectId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Exception with deleted lessons", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, null);
    }

    /**
     * updates lessons data in lw_params table
     *
     * @param objectId
     * @param value
     */
    @Override
    public void updateLessons(int objectId, String value) {
        connection = oracleConnect.connect();
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO lw_params VALUES (?,9,?)");
            preparedStatement.setInt(1, objectId);
            preparedStatement.setString(2, value);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Exception with deleted lessons", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, null);
    }

    /**
     * returns map with object type id and name of all object types
     *
     * @return map
     */
    @Override
    public Map<Integer, String> getAllObjectTypes() {
        LOGGER.debug("Getting object type");
        connection = oracleConnect.connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT object_type_id, name FROM lw_object_types" +
                    " ORDER BY object_type_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(DAOOracleImpl.OBJECT_TYPE_ID), resultSet.getString(DAOOracleImpl.NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting all object types", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return map;
    }

    public void setOracleConnect(OracleConnect oracleConnect) {
        this.oracleConnect = oracleConnect;
    }
}
