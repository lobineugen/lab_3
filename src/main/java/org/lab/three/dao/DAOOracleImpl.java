package org.lab.three.dao;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.log4j.Logger;
import org.lab.three.connect.OracleConnect;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

/**
 * DAOOracleImpl class organizes work with database
 */
@Service
public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    public static final String NAME = "name";
    public static final String OBJECT_ID = "object_id";
    public static final String OBJECT_TYPE_ID = "object_type_id";
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);
    private OracleConnect oracleConnect;

    /**
     * creates object
     *
     * @param name
     * @param parentId
     * @param objectType
     * @return objectID
     */
    @Override
    public int createObject(String name, String parentId, String objectType) {
        LOGGER.debug("Creating object");
        connection = oracleConnect.connect();
        int objectID = getNextId();
        String objectT = objectType;
        String parID = parentId;
        try {
            if ("0".equals(parID)) {
                parID = "null";
            }
            if ("null".equals(objectT)) {
                objectT = "1";
            }
            preparedStatement = connection.prepareStatement("INSERT INTO LW_OBJECTS VALUES (?,?,?,?)");
            preparedStatement.setInt(1, objectID);
            if ("null".equals(parID)) {
                preparedStatement.setNull(2, Types.INTEGER);
            } else {
                preparedStatement.setInt(2, Integer.parseInt(parID));
            }
            preparedStatement.setInt(3, Integer.parseInt(objectT));
            preparedStatement.setString(4, name);

            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception while creating object", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return objectID;
    }

    /**
     * returns HashMap with OBJECT_TYPE_ID and OBJECT_TYPE
     *
     * @param parentId
     * @return map
     */
    @Override
    public Map<Integer, String> getObjectTypes(int parentId) {
        LOGGER.debug("Getting object type");
        connection = oracleConnect.connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT object_type_id,name FROM lw_object_types " +
                    "WHERE parent_id = (SELECT object_type_id FROM lw_objects WHERE object_id = ?)");
            preparedStatement.setInt(1, parentId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(OBJECT_TYPE_ID), resultSet.getString(NAME));
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object type", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return map;
    }

    /**
     * executes SQL script
     */
    @Override
    public void executeScript(Connection connection) {
        LOGGER.debug("Executing script");
        URL script = DAOOracleImpl.class.getClassLoader().getResource("script.sql");
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        try {
            Reader reader = new BufferedReader(new FileReader(script.getPath()));
            scriptRunner.setSendFullScript(true);
            scriptRunner.runScript(reader);
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException while executing script", e);
        }
    }


    /**
     * returns list of object attribute IDs
     *
     * @param objectId
     * @return list
     */
    @Override
    public ArrayList<Integer> getAttrByObjectIdFromParams(int objectId) {
        connection = oracleConnect.connect();
        ArrayList<Integer> arrayList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT attr_id FROM lw_aot " +
                    "WHERE object_type_id = (SELECT object_type_id FROM lw_objects WHERE object_id = ?)");
            preparedStatement.setInt(1, objectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arrayList.add(resultSet.getInt("attr_id"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get attr by object id", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return arrayList;
    }

    /**
     * updates object parameters table data
     *
     * @param objectId
     * @param attrID
     * @param value
     */
    @Override
    public void updateParams(int objectId, int attrID, String value) {
        connection = oracleConnect.connect();
        try {
            if (!value.isEmpty()) {
                preparedStatement = connection.prepareStatement("MERGE INTO lw_params p " +
                        "USING (SELECT object_id FROM lw_objects WHERE object_id = ?) s " +
                        "ON (p.object_id = s.object_id AND p.attr_id = ?) " +
                        "WHEN MATCHED THEN UPDATE SET p.value = ?" +
                        "WHEN NOT MATCHED THEN INSERT VALUES (s.object_id,? , ?)");
                preparedStatement.setInt(1, objectId);
                preparedStatement.setInt(2, attrID);
                preparedStatement.setString(3, value);
                preparedStatement.setInt(4, attrID);
                preparedStatement.setString(5, value);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Exception update params", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
    }

    /**
     * returns data from lw_attr table by objectType
     *
     * @param objectType
     * @return map
     */
    @Override
    public Map<Integer, String> getAttrByObjectIdFromAOT(int objectType) {
        connection = oracleConnect.connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT a.attr_id, a.name FROM lw_aot aot, lw_attr a\n " +
                    " WHERE aot.object_type_id = ? AND a.attr_id = aot.attr_id");
            preparedStatement.setInt(1, objectType);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("attr_id"), resultSet.getString(NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception update params", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return map;
    }

    /**
     * returns next ID
     *
     * @return id
     */
    @Override
    public int getNextId() {
        int id = 0;
        try {
            preparedStatement = connection.prepareStatement("SELECT sequence_next_id.nextval AS id FROM dual");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get unique id", e);
        }
        return id;
    }

    /**
     * returns role right by role name
     *
     * @param name
     * @return role right
     */
    @Override
    public String getRightByUserName(String name) {
        connection = oracleConnect.connect();
        String right = "";
        try {
            preparedStatement = connection.prepareStatement("SELECT right FROM lw_right WHERE name = ?");
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                right = resultSet.getString("right");
            }
            if (right.isEmpty()) {
                preparedStatement = null;
                preparedStatement = connection.prepareStatement("INSERT INTO LW_RIGHT VALUES ( ? ,  '" + name + "' , 'INFO')");
                preparedStatement.setInt(1, getNextId());
                preparedStatement.executeUpdate();
                right = "INFO";
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select right for user", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return right;
    }

    /**
     * returns object name by object id
     *
     * @param objectId
     * @return object name
     */
    @Override
    public String getNameById(int objectId) {
        connection = oracleConnect.connect();
        String name = "";
        try {
            preparedStatement = connection.prepareStatement("SELECT name FROM lw_objects WHERE object_id = ?");
            preparedStatement.setInt(1, objectId);
            while (preparedStatement.executeQuery().next()) {
                name = resultSet.getString(NAME);
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select right for user", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return name;
    }

    /**
     * returns path to object in map of object id and name
     *
     * @param parentID
     * @return map
     */
    @Override
    public Map<Integer, String> getPath(int parentID) {
        connection = oracleConnect.connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT level, o.object_id, o.name, o.object_type_id FROM lw_objects o START WITH o.object_id = ? CONNECT BY PRIOR  o.parent_id = o.object_id\n" +
                    "ORDER BY level DESC");
            preparedStatement.setInt(1, parentID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("object_type_id") != 1) {
                    map.put(resultSet.getInt("object_id"),
                            resultSet.getString(NAME));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with get path", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return map;
    }


    public void setOracleConnect(OracleConnect oracleConnect) {
        this.oracleConnect = oracleConnect;
    }
}