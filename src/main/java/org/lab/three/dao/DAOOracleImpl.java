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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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

    public List<LWObject> getChildren(int object_id) {
        LOGGER.debug("Getting children objects");
        connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select * from LW_OBJECTS where PARENT_ID = " + object_id);
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
    public List<LWObject> removeByID(int[] object_id, String parent_id) {
        LOGGER.debug("Removing object by ID");
        connect();
        List<LWObject> list = new ArrayList<>();
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
                preparedStatement.executeUpdate();
            }
            preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = " + parent_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
            String quer = "select * from lw_objects " +
                    "where parent_id = (select parent_id from lw_objects where object_id = " + parent_id + ")";
            if (parent_id.equals("0")) {
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
    public int createObject(String name, String parentId, String objectType) {
        LOGGER.debug("Creating object");
        connect();
        int object_id = getNextId();
        try {
            if (parentId.equals("0")) {
                parentId = "null";
            }
            if (objectType.equals("null")) {
                objectType = "1";
            }
            preparedStatement = connection.prepareStatement("insert into LW_OBJECTS VALUES (" + object_id + "," + parentId + "," + objectType + ",'" + name + "')");
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception while creating object", e);
        }
        disconnect();
        return object_id;
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
    public LWObject getObjectById(int objectId) {
        LOGGER.debug("Getting object by ID");
        connect();
        LWObject LWObject = null;
        try {
            preparedStatement = connection.prepareStatement("select * from LW_OBJECTS where OBJECT_ID=" + objectId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LWObject = parseObject(resultSet);
            }

        } catch (SQLException e) {
            LOGGER.error("Exception while getting object by ID", e);
        }
        disconnect();
        return LWObject;
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
        ScriptRunner sr = new ScriptRunner(connection, false, false);
        try {
            Reader reader = new BufferedReader(new FileReader(script.getPath()));
            sr.runScript(reader);
        } catch (Exception e) {
            LOGGER.error("Exception while executing script", e);
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
                arrays.put(resultSet.getInt("attr_id") + "_" + resultSet.getString("name"),
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
    public void updateParams(int objectId, int attr_id, String value) {
        connect();
        try {
            if (!value.isEmpty()) {
                preparedStatement = connection.prepareStatement("MERGE INTO lw_params p " +
                        "USING (SELECT object_id FROM lw_objects WHERE object_id = " + objectId + ") s " +
                        "ON (p.object_id = s.object_id AND p.attr_id = " + attr_id + ") " +
                        "WHEN MATCHED THEN UPDATE SET p.value = '" + value + "'" +
                        " WHEN NOT MATCHED THEN INSERT VALUES (s.object_id," + attr_id + ", '" + value + "')");
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Exception update params", e);
        }
        disconnect();
    }

    @Override
    public Map<Integer, String> getAttrByObjectIdFromAOT(int object_type) {
        connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("select a.attr_id, a.name from lw_aot aot, lw_attr a\n " +
                    " where aot.object_type_id = " + object_type +
                    " and a.attr_id = aot.attr_id");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("attr_id"), resultSet.getString("name"));
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
                int object_type_id = 0;
                while (resultSet.next()) {
                    object_type_id = resultSet.getInt("object_type_id");
                }
                disconnect();
                if (object_type_id == 1) {
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
                objects.put(resultSet.getInt("object_id"), resultSet.getString("name"));
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
                map.put(resultSet.getInt("object_id"),
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
                list.add(new Visit(resultSet.getInt("object_id"),
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
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
        int object_id = resultSet.getInt("object_id");
        Map<String, String> map = getParamsById(object_id);
        return new LWObject(object_id,
                resultSet.getInt("parent_id"),
                resultSet.getInt("object_type_id"),
                resultSet.getString("name"), map);
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
                map.put(resultSet.getInt("object_type_id"), resultSet.getString("name"));
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
