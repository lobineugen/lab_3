package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public void connect() {
        Hashtable ht = new Hashtable();
        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        ht.put(Context.PROVIDER_URL, "t3://localhost:7001");
        try {
            Context ctx = new InitialContext(ht);
            DataSource ds = (DataSource) ctx.lookup("datasourceLab");
            connection = ds.getConnection();
            if (!connection.isClosed()) {
                System.out.println("Connection successful");
            }
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null)
                connection.close();
            if (preparedStatement != null)
                preparedStatement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<lwObject> getTopObject() {
        connect();
        List<lwObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.* FROM LW_OBJECTS o WHERE o.object_type_id = 1");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        disconnect();
        return list;
    }

    public List<lwObject> getChildren(int object_id) {
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
            e.printStackTrace();
        }

        disconnect();
        return list;
    }

    @Override
    public List<lwObject> removeByID(int[] object_id, String parent_id) {
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
            e.printStackTrace();
        }
        disconnect();
        return list;
    }

    @Override
    public void createObject(String name, String parentId, String objectType) {
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
            e.printStackTrace();
        }
        disconnect();
    }

    @Override
    public Map<Integer, String> getObjectTypes(int parentId) {
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
            e.printStackTrace();
        }
        disconnect();
        return map;
    }

    @Override
    public lwObject getObjectById(int objectId) {
        connect();
        lwObject lwObject = null;
        try {
            preparedStatement = connection.prepareStatement("select * from LW_OBJECTS where OBJECT_ID=" + objectId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                lwObject = parseObject(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
        return lwObject;
    }

    @Override
    public List<lwObject> changeNameById(int objectId, String name) {
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
            e.printStackTrace();
        }
        disconnect();
        return list;
    }

    private lwObject parseObject(ResultSet resultSet) throws SQLException {
        return new lwObject(resultSet.getInt("object_id"),
                resultSet.getInt("parent_id"),
                resultSet.getInt("object_type_id"),
                resultSet.getString("name"));
    }
}
