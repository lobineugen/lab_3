package org.lab.three.dao;

import com.ibatis.common.jdbc.ScriptRunner;
import org.apache.log4j.Logger;
import org.lab.three.beans.LWObject;
import org.lab.three.beans.Visit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final String NAME = "name";
    private static final String OBJECT_ID = "object_id";
    private static final String OBJECT_TYPE_ID = "object_type_id";
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);

    public void connect() {
        LOGGER.debug("Connecting to database");
        Hashtable hashtable = new Hashtable();
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        hashtable.put(Context.PROVIDER_URL, "t3://localhost:7001");
        try {
            Context context = new InitialContext(hashtable);
            DataSource dataSource = (DataSource) context.lookup("datasourceLab");
            connection = dataSource.getConnection();
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

    public List<LWObject> getTopObject() {
        LOGGER.debug("Getting top objects");
        connect();
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

        disconnect();
        return list;
    }

    public List<LWObject> getChildren(int objectID) {
        LOGGER.debug("Getting children objects");
        connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select * from LW_OBJECTS where PARENT_ID = " + objectID);
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
    public List<LWObject> removeByID(int[] objectID, String parentID) {
        LOGGER.debug("Removing object by ID");
        connect();
        List<LWObject> list = new ArrayList<>();
        StringBuilder query = new StringBuilder("(");
        for (int i = 0; i < objectID.length; i++) {
            query.append(objectID[i]);
            if (i != objectID.length - 1) {
                query.append(',');
            }
        }
        query.append(')');
        try {
            if (objectID.length > 0) {
                preparedStatement = connection.prepareStatement("delete from lw_objects where object_id in " + query);
                preparedStatement.executeUpdate();
            }
            preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = " + parentID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
            String quer = "select * from lw_objects " +
                    "where parent_id = (select parent_id from lw_objects where object_id = " + parentID + ")";
            if ("0".equals(parentID)) {
                quer = "SELECT * FROM lw_objects " +
                        "WHERE parent_id IS NULL ";
            }
            if (list.isEmpty()) {
                preparedStatement = connection.prepareStatement(quer);
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
        disconnect();
        return list;
    }

    @Override
    public int createObject(String name, String parentId, String objectType) {
        LOGGER.debug("Creating object");
        connect();
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
            preparedStatement = connection.prepareStatement("insert into LW_OBJECTS VALUES (" + objectID +
                    "," + parID + "," + objectT + ",'" + name + "')");
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception while creating object", e);
        }
        disconnect();
        return objectID;
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
                map.put(resultSet.getInt(OBJECT_TYPE_ID), resultSet.getString(NAME));
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object type", e);
        }
        disconnect();
        return map;
    }

    @Override
    public LWObject getObjectById(int objectId) {
        LOGGER.debug("Getting object by ID");
        connect();
        LWObject lwObject = null;
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
    public List<LWObject> changeNameById(int objectId, String name) {
        LOGGER.debug("Changing name by ID");
        connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("update lw_objects set name='" + name + "' where OBJECT_ID = " + objectId);
            preparedStatement.executeUpdate();
            list = getObjectsListByObject(objectId);
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
            preparedStatement = connection.prepareStatement("SELECT count(table_name) AS counts FROM user_tables WHERE table_name IN ('LW_PARAMS','LW_AOT','LW_ATTR','LW_OBJECTS','LW_OBJECT_TYPES','LW_VISIT')");
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
        ScriptRunner scriptRunner = new ScriptRunner(connection, false, false);
        try {
            Reader reader = new BufferedReader(new FileReader(script.getPath()));
            scriptRunner.runScript(reader);
        } catch (SQLException e) {
            LOGGER.error("SQLException while executing script", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException while executing script", e);
        } catch (IOException e) {
            LOGGER.error("IOException while executing script", e);
        }
        disconnect();
    }

    @Override
    public Map<String, String> getParamsById(int objectId) {
        Map<String, String> arrays = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("select a.attr_id,a.name, p.value from lw_params p, lw_attr a where a.attr_id = p.attr_id  and object_id = " + objectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arrays.put(resultSet.getInt("attr_id") + "_" + resultSet.getString(NAME),
                        resultSet.getString("value"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get params by id ", e);
        }
        return arrays;
    }

    @Override
    public ArrayList<Integer> getAttrByObjectIdFromParams(int objectId) {
        connect();
        ArrayList<Integer> arrayList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select attr_id from lw_aot " +
                    "where object_type_id = (select object_type_id from lw_objects where object_id = " + objectId + ")");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arrayList.add(resultSet.getInt("attr_id"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get attr by object id", e);
        }
        disconnect();
        return arrayList;
    }

    @Override
    public void updateParams(int objectId, int attrID, String value) {
        connect();
        try {
            if (!value.isEmpty()) {
                preparedStatement = connection.prepareStatement("MERGE INTO lw_params p " +
                        "USING (SELECT object_id FROM lw_objects WHERE object_id = " + objectId + ") s " +
                        "ON (p.object_id = s.object_id AND p.attr_id = " + attrID + ") " +
                        "WHEN MATCHED THEN UPDATE SET p.value = '" + value + "'" +
                        " WHEN NOT MATCHED THEN INSERT VALUES (s.object_id," + attrID + ", '" + value + "')");
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Exception update params", e);
        }
        disconnect();
    }

    @Override
    public Map<Integer, String> getAttrByObjectIdFromAOT(int objectType) {
        connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("select a.attr_id, a.name from lw_aot aot, lw_attr a\n " +
                    " where aot.object_type_id = " + objectType +
                    " and a.attr_id = aot.attr_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("attr_id"), resultSet.getString(NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception update params", e);
        }
        disconnect();
        return map;
    }

    @Override
    public int getNextId() {
        int id = 0;
        try {
            preparedStatement = connection.prepareStatement("SELECT sss.nextval AS id FROM dual");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get unique id", e);
        }
        return id;
    }

    @Override
    public List<LWObject> getObjectsListByObject(int objectId) {

        List<LWObject> list = new ArrayList<>();
        try {
            if (objectId == 0) {
                list = getTopObject();
            } else {
                connect();
                preparedStatement = connection.prepareStatement("select object_type_id from LW_OBJECTS where OBJECT_ID = " + objectId);
                resultSet = preparedStatement.executeQuery();
                int objectTypeID = 0;
                while (resultSet.next()) {
                    objectTypeID = resultSet.getInt(OBJECT_TYPE_ID);
                }
                disconnect();
                if (objectTypeID == 1) {
                    list = getTopObject();
                } else {
                    connect();
                    preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = (select parent_id from lw_objects where object_id = " + objectId + ")");
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        list.add(parseObject(resultSet));
                    }
                    disconnect();
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Exception get objects ", e);
        }
        disconnect();
        return list;
    }

    @Override
    public Map<Integer, String> getObjectsByObjectType(int objectType) {
        Map<Integer, String> objects = new HashMap<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("select object_id, name from LW_OBJECTS where OBJECT_TYPE_ID = " + objectType);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                objects.put(resultSet.getInt(OBJECT_ID), resultSet.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get objects by object type id ", e);
        }
        disconnect();
        return objects;
    }

    @Override
    public Map<Integer, String> getStudentsByLessonId(int lessonId) {
        Map<Integer, String> map = new HashMap<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.object_id, o.name " +
                    "FROM lw_params p, lw_objects o " +
                    "WHERE p.attr_id = 9 AND p.value = '" + lessonId + "' AND p.object_id = o.object_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(OBJECT_ID),
                        resultSet.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception get students by lesson id ", e);
        }
        disconnect();
        return map;
    }

    @Override
    public void insertVisit(String lessonId, String objectId, String date, String value) {
        connect();
        try {
            preparedStatement = connection.prepareStatement("MERGE INTO lw_visit d\n" +
                    "USING (SELECT " + objectId + " AS object_id, " + lessonId + " AS lesson_id, '" + date + "' AS editDate, '" + value + "' AS mark FROM dual) s\n" +
                    "ON (d.object_id = s.object_id AND d.lesson_id = s.lesson_id AND d.editDate = s.editDate)\n" +
                    "WHEN MATCHED THEN UPDATE SET d.mark = s.mark\n" +
                    "WHEN NOT MATCHED THEN INSERT VALUES (s.object_id,s.lesson_id, s.editDate, s.mark)");
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception with merge into visit", e);
        }
        disconnect();
    }

    @Override
    public List<Visit> getVisitByLessonId(int lessonId) {
        List<Visit> list = new ArrayList<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("select OBJECT_ID,LESSON_ID,EDITDATE,MARK from lw_visit where lesson_id = " + lessonId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(new Visit(resultSet.getInt(OBJECT_ID),
                        resultSet.getInt("lesson_id"),
                        resultSet.getString("editDate"),
                        resultSet.getString("mark")));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select from visit", e);
        }
        disconnect();
        return list;
    }

    @Override
    public List<String> getDistinctDateByLessonId(int lessonId) {
        connect();
        List<String> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select distinct editDate from lw_visit where LESSON_ID=" + lessonId + " order by to_date(editDate,'dd.mm.yyyy')");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString("editDate"));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with select from visit", e);
        }
        disconnect();
        return list;
    }

    private LWObject parseObject(ResultSet resultSet) throws SQLException {
        LOGGER.debug("Parsing object");
        int objectID = resultSet.getInt(OBJECT_ID);
        Map<String, String> map = getParamsById(objectID);
        return new LWObject(objectID,
                resultSet.getInt("parent_id"),
                resultSet.getInt(OBJECT_TYPE_ID),
                resultSet.getString(NAME), map);
    }

    @Override
    public Map<Integer, String> getAllObjectTypes() {
        LOGGER.debug("Getting object type");
        connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT object_type_id, name FROM lw_object_types" +
                    " ORDER BY object_type_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt(OBJECT_TYPE_ID), resultSet.getString(NAME));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting all object types", e);
        }
        disconnect();
        return map;
    }

    @Override
    public List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID) {
        LOGGER.debug("Getting object");
        connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select * from lw_objects where" +
                    " name like '%" + objectName + "%' and object_type_id = '" + objectTypeID + "'");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while getting object", e);
        }
        disconnect();
        return list;
    }
}
