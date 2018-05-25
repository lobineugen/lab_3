package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public void connect() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "student");
            if (!connection.isClosed()) {
                System.out.println("Connection successful");
            }
        } catch (ClassNotFoundException | SQLException e) {
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

    public List<lwObject> getChildren(int object_id){
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
    public List<lwObject> removeByID(int[] object_id,int parent_id) {
        connect();
        List<lwObject> list = new ArrayList<>();
        StringBuilder query = new StringBuilder("(");
        for (int i = 0; i <object_id.length; i++) {
            query.append(object_id[i]);
            if (i!=object_id.length-1){
                query.append(",");
            }
        }
        query.append(")");
        try {
            if (object_id.length>0){
                preparedStatement = connection.prepareStatement("delete from lw_objects where object_id in " + query);
                int count = preparedStatement.executeUpdate();
            }
            preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = " + parent_id);
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
    public void createObject(String name, String parentId, String objectType) {
        connect();
        try {
            if (parentId.equals("0")){
                parentId = "null";
            }
            if (objectType.equals("null")){
                objectType = "1";
            }
            preparedStatement = connection.prepareStatement("insert into LW_OBJECTS VALUES (sss.nextVal,"+parentId+","+objectType+",'"+name+"')");
            resultSet = preparedStatement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }

    @Override
    public Map<Integer, String> getObjectTypes(int parentId) {
        connect();
        Map<Integer,String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("select object_type_id,name from lw_object_types " +
                    "where parent_id = (select object_type_id from lw_objects where object_id = "+parentId+")");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("object_type_id"),resultSet.getString("name"));
            }
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
        return map;
    }

    private lwObject parseObject(ResultSet resultSet) throws SQLException {
        return new lwObject(resultSet.getInt("object_id"),
                resultSet.getInt("parent_id"),
                resultSet.getInt("object_type_id"),
                resultSet.getString("name"));
    }
}
