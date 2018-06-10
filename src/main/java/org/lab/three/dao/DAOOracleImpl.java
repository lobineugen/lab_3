package org.lab.three.dao;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

/**
 * DAOOracleImpl class organizes work with database
 */
public class DAOOracleImpl implements DAO {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static final String NAME = "name";
    private static final String OBJECT_ID = "object_id";
    private static final String OBJECT_TYPE_ID = "object_type_id";
    private static String datasourceName;
    private static final String DATASOURCE_PROPERTIES_PATH = "application.properties";
    private static final Logger LOGGER = Logger.getLogger(DAOOracleImpl.class);

    /**
     * connects to database
     */
    public void connect() {
        LOGGER.debug("Connecting to database");
        uploadDatasourceProperties();
        Hashtable hashtable = new Hashtable();
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        hashtable.put(Context.PROVIDER_URL, "t3://localhost:7001");
        try {
            Context context = new InitialContext(hashtable);
            DataSource dataSource = (DataSource) context.lookup(datasourceName);
            connection = dataSource.getConnection();
            if (!connection.isClosed()) {
                LOGGER.info("Connection successful");
            }
        } catch (NamingException | SQLException e) {
            LOGGER.error("Exception during connection to database", e);
        }
    }

    /**
     * uploads datasourceName name from file
     */
    private void uploadDatasourceProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream socketProperties = classLoader.getResourceAsStream(DATASOURCE_PROPERTIES_PATH);
        Properties properties = new Properties();
        try {
            properties.load(socketProperties);
        } catch (IOException e) {
            LOGGER.error("Can't load application.properties", e);
        }
        datasourceName = properties.getProperty("datasourceName");
    }

    /**
     * disconnects from database
     */
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

    /**
     * returns list of top objects
     * @return list
     */
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

    /**
     * returns list of child objects
     * @param objectID
     * @return list
     */
    public List<LWObject> getChildren(int objectID) {
        LOGGER.debug("Getting children objects");
        connect();
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

        disconnect();
        return list;
    }

    /**
     * removes object by ID
     * @param objectID
     * @param parentID
     * @return list
     */
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
            preparedStatement = connection.prepareStatement("SELECT * FROM lw_objects WHERE parent_id = ?");
            preparedStatement.setInt(1, Integer.parseInt(parentID));
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

    /**
     * creates object
     * @param name
     * @param parentId
     * @param objectType
     * @return objectID
     */
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
            preparedStatement = connection.prepareStatement("INSERT INTO LW_OBJECTS VALUES (?," + parID + ",?,?)");
            preparedStatement.setInt(1, objectID);
            preparedStatement.setInt(2, Integer.parseInt(objectT));
            preparedStatement.setString(3, name);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Exception while creating object", e);
        }
        disconnect();
        return objectID;
    }

    /**
     * returns HashMap with OBJECT_TYPE_ID and OBJECT_TYPE
     * @param parentId
     * @return map
     */
    @Override
    public Map<Integer, String> getObjectTypes(int parentId) {
        LOGGER.debug("Getting object type");
        connect();
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
        disconnect();
        return map;
    }

    /**
     * returns object by ID
     * @param objectId
     * @return object
     */
    @Override
    public LWObject getObjectById(int objectId) {
        LOGGER.debug("Getting object by ID");
        connect();
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
        disconnect();
        return lwObject;
    }

    /**
     * changes object name by ID
     * @param objectId
     * @param name
     * @return list
     */
    @Override
    public List<LWObject> changeNameById(int objectId, String name) {
        LOGGER.debug("Changing name by ID");
        connect();
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
        disconnect();
        return list;
    }

    /**
     * checks table number
     * @return number of tables
     */
    @Override
    public int checkTables() {
        LOGGER.debug("Checking tables");
        connect();
        int count = 0;
        try {
            preparedStatement = connection.prepareStatement("SELECT count(table_name) AS counts FROM user_tables WHERE table_name IN ('LW_PARAMS','LW_AOT','LW_ATTR','LW_OBJECTS','LW_OBJECT_TYPES','LW_VISIT','LW_RIGHT')");
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

    /**
     * executes SQL script
     */
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

    /**
     * returns collection of all object parameters
     * @param objectId
     * @return collection
     */
    @Override
    public Multimap<String, String> getParamsById(int objectId) {
        Multimap<String, String> arrays = ArrayListMultimap.create();
        try {
            preparedStatement = connection.prepareStatement("SELECT a.attr_id,a.name, p.value FROM lw_params p, lw_attr a WHERE a.attr_id = p.attr_id  AND object_id = ?");
            preparedStatement.setInt(1, objectId);
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

    /**
     * returns list of object attribute IDs
     * @param objectId
     * @return list
     */
    @Override
    public ArrayList<Integer> getAttrByObjectIdFromParams(int objectId) {
        connect();
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
        disconnect();
        return arrayList;
    }

    /**
     * updates object parameters table data
     * @param objectId
     * @param attrID
     * @param value
     */
    @Override
    public void updateParams(int objectId, int attrID, String value) {
        connect();
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
        disconnect();
    }

    /**
     * returns data from lw_attr table by objectType
     * @param objectType
     * @return map
     */
    @Override
    public Map<Integer, String> getAttrByObjectIdFromAOT(int objectType) {
        connect();
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
        disconnect();
        return map;
    }

    /**
     * returns next ID
     * @return id
     */
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

    /**
     * returns objects list by object
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
                connect();
                preparedStatement = connection.prepareStatement("SELECT object_type_id FROM LW_OBJECTS WHERE OBJECT_ID = ?");
                preparedStatement.setInt(1, objectId);
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
                    disconnect();
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Exception get objects ", e);
        }
        disconnect();
        return list;
    }

    /**
     * returns objects id and name map by object type
     * @param objectType
     * @return map
     */
    @Override
    public Map<Integer, String> getObjectsByObjectType(int objectType) {
        Map<Integer, String> objects = new HashMap<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT object_id, name FROM LW_OBJECTS WHERE OBJECT_TYPE_ID = ?");
            preparedStatement.setInt(1, objectType);
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

    /**
     * returns students id and name map for particular lesson
     * @param lessonId
     * @return map
     */
    @Override
    public Map<Integer, String> getStudentsByLessonId(int lessonId) {
        Map<Integer, String> map = new HashMap<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT o.object_id, o.name " +
                    "FROM lw_params p, lw_objects o " +
                    "WHERE p.attr_id = 9 AND p.value = ? AND p.object_id = o.object_id");
            preparedStatement.setString(1, String.valueOf(lessonId));
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

    /**
     * inserts data into lw_visit table
     * @param lessonId
     * @param objectId
     * @param date
     * @param value
     */
    @Override
    public void insertVisit(String lessonId, String objectId, String date, String value) {
        connect();
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
        disconnect();
    }

    /**
     * returns list of visits by lesson id
     * @param lessonId
     * @return list
     */
    @Override
    public List<Visit> getVisitByLessonId(int lessonId) {
        List<Visit> list = new ArrayList<>();
        connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT OBJECT_ID,LESSON_ID,EDITDATE,MARK FROM lw_visit WHERE lesson_id = ?");
            preparedStatement.setInt(1, lessonId);
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

    /**
     * returns list of unique dates by lesson id
     * @param lessonId
     * @return list
     */
    @Override
    public List<String> getDistinctDateByLessonId(int lessonId) {
        connect();
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
        disconnect();
        return list;
    }

    /**
     * returns role right by role name
     * @param name
     * @return role right
     */
    @Override
    public String getRightByUserName(String name) {
        connect();
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
        disconnect();
        return right;
    }

    /**
     * returns object name by object id
     * @param objectId
     * @return object name
     */
    @Override
    public String getNameById(int objectId) {
        connect();
        String name = "";
        try {
            preparedStatement = connection.prepareStatement("SELECT name FROM lw_objects WHERE object_id = ?");
            preparedStatement.setInt(1, objectId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("name");
            }

        } catch (SQLException e) {
            LOGGER.error("Exception with select right for user", e);
        }
        disconnect();
        return name;
    }

    /**
     * deletes lessons from lw_params table
     * @param objectId
     */
    @Override
    public void deleteAllLessons(int objectId) {
        connect();
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM lw_params WHERE object_id = ? AND attr_id = 9");
            preparedStatement.setInt(1, objectId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Exception with deleted lessons", e);
        }
        disconnect();
    }

    /**
     * updates lessons data in lw_params table
     * @param objectId
     * @param value
     */
    @Override
    public void updateLessons(int objectId, String value) {
        connect();
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO lw_params VALUES (?,9,?)");
            preparedStatement.setInt(1, objectId);
            preparedStatement.setString(2, value);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Exception with deleted lessons", e);
        }
        disconnect();
    }

    /**
     * returns path to object in map of object id and name
     * @param parent_id
     * @return  map
     */
    @Override
    public Map<Integer, String> getPath(int parent_id) {
        connect();
        Map<Integer, String> map = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT level, o.object_id, o.name, o.object_type_id FROM lw_objects o START WITH o.object_id = ? CONNECT BY PRIOR  o.parent_id = o.object_id\n" +
                    "ORDER BY level DESC");
            preparedStatement.setInt(1, parent_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt("object_type_id") != 1) {
                    map.put(resultSet.getInt("object_id"),
                            resultSet.getString("name"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with get path", e);
        }
        disconnect();
        return map;
    }

    /**
     * returns list of parent objects by children
     * @param object_id
     * @return list
     */
    @Override
    public List<LWObject> getParentByChildren(int object_id) {
        connect();
        List<LWObject> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("select * from lw_objects where parent_id = (select parent_id from lw_objects where object_id = ?)");
            preparedStatement.setInt(1, object_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(parseObject(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("Exception with get path", e);
        }
        disconnect();
        return list;
    }

    /**
     * returns new object creating from resultSet
     * @param resultSet
     * @return object
     */
    private LWObject parseObject(ResultSet resultSet) throws SQLException {
        LOGGER.debug("Parsing object");
        int objectID = resultSet.getInt(OBJECT_ID);
        Multimap<String, String> map = getParamsById(objectID);
        return new LWObject(objectID,
                resultSet.getInt("parent_id"),
                resultSet.getInt(OBJECT_TYPE_ID),
                resultSet.getString(NAME), map);
    }

    /**
     * returns map with object type id and name of all object types
     * @return map
     */
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

    /**
     * returns list of objects by list and type
     * @param objectName
     * @param objectTypeID
     * @return list
     */
    @Override
    public List<LWObject> getLWObjectByNameAndType(String objectName, int objectTypeID) {
        LOGGER.debug("Getting object");
        connect();
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
        disconnect();
        return list;
    }
}