package org.lab.three.dao;

import com.ibatis.common.jdbc.ScriptRunner;
import org.apache.log4j.Logger;
import org.lab.three.beans.lwObject;
import org.lab.three.controller.Init;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);

    public void connect() {
        LOGGER.debug("Connecting to database");
        Hashtable ht = new Hashtable();
        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        ht.put(Context.PROVIDER_URL, "t3://localhost:7001");
        try {
            Context ctx = new InitialContext(ht);
            DataSource ds = (DataSource) ctx.lookup("datasourceLab");
            connection = ds.getConnection();
            if (!connection.isClosed()) {
                LOGGER.info("Connection successful");
            }
        } catch (NamingException | SQLException e) {
            LOGGER.error("Exception during connection to database", e);
        }
    }

    public void disconnect() {
        LOGGER.debug("Disconnecting from database");
        try {
            if (connection != null)
                connection.close();
            if (preparedStatement != null)
                preparedStatement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Exception during disconnection from database", e);
        }
    }

    public List<lwObject> getTopObject() {
        LOGGER.debug("Getting top objects");
        connect();
        List<lwObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.* FROM LW_OBJECTS o WHERE o.object_type_id = 1");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting top objects", e);
        }

        disconnect();
        return list;
    }

    public List<lwObject> getChildren(int object_id) {
        LOGGER.debug("Getting children objects");
        connect();
        List<lwObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.* FROM lw_objects o\n" +
                    "WHERE level=2" +
                    "START WITH o.object_id = " + object_id +
                    "CONNECT BY PRIOR o.object_id = o.parent_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting children objects", e);
        }

        disconnect();
        return list;
    }

    @Override
    public List<lwObject> removeByID(int[] object_id, String parent_id) {
        LOGGER.debug("Removing object by ID");
        System.out.println("parent id = " + parent_id);
        connect();
        List<lwObject> list = new ArrayList<>();
        StringBuilder query = new StringBuilder("(");
        for (int i = 0; i < object_id.length; i++) {
            query.append(object_id[i]);
            if (i != object_id.length - 1) {
                query.append(",");
            }
        }
        query.append(")");
        try {
            if (object_id.length > 0) {
                preparedStatement = connection.prepareStatement("delete from lw_objects where object_id in " + query);
                int count = preparedStatement.executeUpdate();
            }
            preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = " + parent_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
            String quer = "select * from lw_objects " +
                    "where parent_id = (select parent_id from lw_objects where object_id = " + parent_id + ")";
            if (parent_id.equals("0")) {
                parent_id = "null";
                quer = "SELECT * FROM lw_objects " +
                        "WHERE parent_id IS NULL ";
            }
            if (list.size() == 0) {
                preparedStatement = connection.prepareStatement(quer);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    list.add(parseObject(resultSet));
                }
                if (list.size() == 0) {
                    list = getTopObject();
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while removing object by ID", e);
        }
        disconnect();
        return list;
    }

    @Override
    public void createObject(String name, String parentId, String objectType) {
        LOGGER.debug("Creating object");
        connect();
        try {
            if (parentId.equals("0")) {
                parentId = "null";
            }
            if (objectType.equals("null")) {
                objectType = "1";
            }
            preparedStatement = connection.prepareStatement("insert into LW_OBJECTS VALUES (sss.nextVal," + parentId + "," + objectType + ",'" + name + "')");
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception while creating object", e);
        }
        disconnect();
    }

    @Override
    public Map<Integer, String> getObjectTypes(int parentId) {
        LOGGER.debug("Getting object type");
        connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("select object_type_id,name from lw_object_types " +
                    "where parent_id = (select object_type_id from lw_objects where object_id = " + parentId + ")");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("object_type_id"), resultSet.getString("name"));
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object type", e);
        }
        disconnect();
        return map;
    }

    @Override
    public lwObject getObjectById(int objectId) {
        LOGGER.debug("Getting object by ID");
        connect();
        lwObject lwObject = null;
        try {
            preparedStatement = connection.prepareStatement("select * from LW_OBJECTS where OBJECT_ID=" + objectId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                lwObject = parseObject(resultSet);
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object by ID", e);
        }
        disconnect();
        return lwObject;
    }

    @Override
    public List<lwObject> changeNameById(int objectId, String name) {
        LOGGER.debug("Changing name by ID");
        connect();
        List<lwObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("update lw_objects set name='" + name + "' where OBJECT_ID = " + objectId);
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("select object_type_id from LW_OBJECTS where OBJECT_ID = " + objectId);
            resultSet = preparedStatement.executeQuery();
            int object_type_id = 0;
            while (resultSet.next()){
                 object_type_id = resultSet.getInt("object_type_id");
            }
            if (object_type_id == 1){
                list = getTopObject();
            } else {
                preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = (select parent_id from lw_objects where object_id = " + objectId + ")");
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    list.add(parseObject(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while changing name by ID", e);
        }
        disconnect();
        return list;
    }

    @Override
    public int checkTables() {
        LOGGER.debug("Checking tables");
        connect();
        int count = 0;
        try {
            preparedStatement = connection.prepareStatement("select count(table_name) as counts from user_tables where table_name in ('LW_PARAMS','LW_AOT','LW_ATTR','LW_OBJECTS','LW_OBJECT_TYPES')");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count = resultSet.getInt("counts");
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while checking tables", e);
        }
        disconnect();
        return count;
    }

    @Override
    public void executeScript() {
        LOGGER.debug("Executing script");
        connect();
        URL script = DAOOracleImpl.class.getClassLoader().getResource("script.sql");
        ScriptRunner sr = new ScriptRunner(connection,false,false);
        try {
            Reader reader = new BufferedReader(new FileReader(script.getPath()));
            sr.runScript(reader);
        } catch (Exception e) {
            LOGGER.error("Exception while executing script", e);
        }
        disconnect();
    }

    private lwObject parseObject(ResultSet resultSet) throws SQLException {
        LOGGER.debug("Parsing object");
        return new lwObject(resultSet.getInt("object_id"),
                resultSet.getInt("parent_id"),
                resultSet.getInt("object_type_id"),
                resultSet.getString("name"));
    }
}
