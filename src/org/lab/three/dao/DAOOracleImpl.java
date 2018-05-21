package org.lab.three.dao;

import org.lab.three.beans.lwObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<lwObject> getObjects() {
        connect();
        List<lwObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM LW_OBJECTS");
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

    private lwObject parseObject(ResultSet resultSet) throws SQLException {
        return new lwObject(resultSet.getInt("object_id"),
                resultSet.getInt("parent_id"),
                resultSet.getInt("object_type_id"),
                resultSet.getString("name"));
    }
}
