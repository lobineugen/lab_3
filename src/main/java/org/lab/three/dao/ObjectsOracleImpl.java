package org.lab.three.dao;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.lab.three.beans.LWObject;
import org.lab.three.connect.OracleConnect;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectsOracleImpl implements Objects {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final Logger LOGGER = Logger.getLogger(ObjectsOracleImpl.class);
    private OracleConnect oracleConnect;

    /**
     * returns list of top objects
     *
     * @return list
     */
    @Override
    public List<LWObject> getTopObject() {
        LOGGER.debug("Getting top objects");
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.* FROM LW_OBJECTS o WHERE o.object_type_id = 1");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting top objects", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns list of child objects
     *
     * @param objectID
     * @return list
     */
    @Override
    public List<LWObject> getChildren(int objectID) {
        LOGGER.debug("Getting children objects");
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.object_id, o.parent_id, o.object_type_id,\n" +
                    "CASE WHEN o.object_type_id = 4 OR o.object_type_id = 5 \n" +
                    "THEN o.name || ' ' || (SELECT value FROM lw_params WHERE attr_id = 4 AND object_id = o.object_id)\n" +
                    "ELSE o.name END AS name\n" +
                    "FROM lw_objects o WHERE o.parent_id = ?");
            preparedStatement.setInt(1, objectID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting children objects", e);
        }

        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns collection of all object parameters
     *
     * @param objectId
     * @return collection
     */
    public Multimap<String, String> getParamsById(int objectId) {
        Multimap<String, String> arrays = ArrayListMultimap.create();
        try {
            preparedStatement = connection.prepareStatement("SELECT a.attr_id,a.name, p.value FROM lw_params p, lw_attr a WHERE a.attr_id = p.attr_id  AND object_id = ?");
            preparedStatement.setInt(1, objectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arrays.put(resultSet.getInt("attr_id") + "_" + resultSet.getString(DAOOracleImpl.NAME),
                        resultSet.getString("value"));
            }

        } catch (SQLException e) {
            LOGGER.error("Exception get params by id ", e);
        }
        return arrays;
    }

    /**
     * returns object by ID
     *
     * @param objectId
     * @return object
     */
    @Override
    public LWObject getObjectById(int objectId) {
        LOGGER.debug("Getting object by ID");
        connection = oracleConnect.connect();
        LWObject lwObject = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM LW_OBJECTS WHERE OBJECT_ID=?");
            preparedStatement.setInt(1, objectId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                lwObject = parseObject(resultSet);
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object by ID", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return lwObject;
    }

    /**
     * returns new object creating from resultSet
     *
     * @param resultSet
     * @return object
     */
    public LWObject parseObject(ResultSet resultSet) throws SQLException {
        LOGGER.debug("Parsing object");
        int objectID = resultSet.getInt(DAOOracleImpl.OBJECT_ID);
        Multimap<String, String> map = getParamsById(objectID);
        return new LWObject(objectID,
                resultSet.getInt("parent_id"),
                resultSet.getInt(DAOOracleImpl.OBJECT_TYPE_ID),
                resultSet.getString(DAOOracleImpl.NAME), map);
    }

    /**
     * removes object by ID
     *
     * @param objectID
     * @param parentID
     * @return list
     */
    @Override
    public List<LWObject> removeByID(int[] objectID, String parentID) {
        LOGGER.debug("Removing object by ID");
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < objectID.length; i++) {
            if (i == objectID.length - 1) {
                query.append("?");
            } else {
                query.append("?,");
            }
        }
        try {
            if (objectID.length > 0) {
                preparedStatement = connection.prepareStatement("DELETE FROM lw_objects WHERE object_id IN (" + query.toString() + ")");
                for (int i = 1; i <= objectID.length; i++) {
                    preparedStatement.setInt(i, objectID[i - 1]);
                }
                preparedStatement.executeUpdate();
            }
            preparedStatement = connection.prepareStatement("SELECT * FROM lw_objects WHERE parent_id = ?");
            preparedStatement.setInt(1, Integer.parseInt(parentID));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
            String quer = "SELECT * FROM lw_objects " +
                    "WHERE parent_id = (SELECT parent_id FROM lw_objects WHERE object_id = ?)";
            boolean nUll = false;
            if ("0".equals(parentID)) {
                quer = "SELECT * FROM lw_objects " +
                        "WHERE parent_id IS NULL ";
                nUll = true;
            }
            if (list.isEmpty()) {
                preparedStatement = connection.prepareStatement(quer);
                if (!nUll) {
                    preparedStatement.setInt(1, Integer.parseInt(parentID));
                }
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    list.add(parseObject(resultSet));
                }
                if (list.isEmpty()) {
                    list = getTopObject();
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while removing object by ID", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * changes object name by ID
     *
     * @param objectId
     * @param name
     * @return list
     */
    @Override
    public List<LWObject> changeNameById(int objectId, String name) {
        LOGGER.debug("Changing name by ID");
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("UPDATE lw_objects SET name=? WHERE OBJECT_ID = ?");
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, objectId);
            preparedStatement.executeUpdate();
            list = getObjectsListByObject(objectId);
        } catch (SQLException e) {
            LOGGER.error("Exception while changing name by ID", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns objects list by object
     *
     * @param objectId
     * @return list
     */
    @Override
    public List<LWObject> getObjectsListByObject(int objectId) {

        List<LWObject> list = new ArrayList<>();
        try {
            if (objectId == 0) {
                list = getTopObject();
            } else {
                connection = oracleConnect.connect();
                preparedStatement = connection.prepareStatement("SELECT object_type_id FROM LW_OBJECTS WHERE OBJECT_ID = ?");
                preparedStatement.setInt(1, objectId);
                resultSet = preparedStatement.executeQuery();
                int objectTypeID = 0;
                while (resultSet.next()) {
                    objectTypeID = resultSet.getInt(DAOOracleImpl.OBJECT_TYPE_ID);
                }
                oracleConnect.disconnect(connection, preparedStatement, resultSet);
                if (objectTypeID == 1) {
                    list = getTopObject();
                } else {
                    connection = oracleConnect.connect();
                    preparedStatement = connection.prepareStatement("SELECT o.object_id, o.parent_id, o.object_type_id,\n" +
                            "CASE WHEN o.object_type_id = 4 OR o.object_type_id = 5\n" +
                            "THEN o.name || ' ' || (SELECT value FROM lw_params WHERE attr_id = 4 AND object_id = o.object_id)\n" +
                            "ELSE o.name END AS name\n" +
                            "FROM lw_objects o WHERE o.parent_id = (SELECT parent_id FROM lw_objects WHERE object_id = ?)");
                    preparedStatement.setInt(1, objectId);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        list.add(parseObject(resultSet));
                    }
                    oracleConnect.disconnect(connection, preparedStatement, resultSet);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Exception get objects ", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns list of parent objects by children
     *
     * @param objectID
     * @return list
     */
    @Override
    public List<LWObject> getParentByChildren(int objectID) {
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM lw_objects WHERE parent_id = (SELECT parent_id FROM lw_objects WHERE object_id = ?)");
            preparedStatement.setInt(1, objectID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with get path", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    /**
     * returns list of objects by list and type
     *
     * @param objectName
     * @param objectTypeID
     * @return list
     */
    @Override
    public List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID) {
        LOGGER.debug("Getting object");
        connection = oracleConnect.connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM (\n" +
                    "SELECT o.object_id, o.parent_id, o.object_type_id,\n" +
                    "CASE WHEN o.object_type_id = 4 OR o.object_type_id = 5 \n" +
                    "THEN o.name || ' ' || (SELECT value FROM lw_params WHERE attr_id = 4 AND object_id = o.object_id) \n" +
                    "ELSE o.name END AS name \n" +
                    "FROM lw_objects o \n" +
                    ") WHERE object_type_id = ? AND upper(name) LIKE '%'||upper(?)||'%'");
            preparedStatement.setString(1, String.valueOf(objectTypeID));
            preparedStatement.setString(2, objectName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting object", e);
        }
        oracleConnect.disconnect(connection, preparedStatement, resultSet);
        return list;
    }

    public void setOracleConnect(OracleConnect oracleConnect) {
        this.oracleConnect = oracleConnect;
    }
}
