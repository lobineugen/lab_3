package org.lab.three.connect;

import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

public class OracleConnect {
    private static final Logger LOGGER = Logger.getLogger(OracleConnect.class);
    private static String datasourceName;
    private static final String DATASOURCE_PROPERTIES_PATH = "application.properties";

    /**
     * connects to database
     */
    public Connection connect() {
        LOGGER.debug("Connecting to database");
        Connection connection = null;
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
        return connection;
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
    public void disconnect(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        LOGGER.debug("Disconnecting from database");
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            LOGGER.error("Exception during disconnection from database", e);
        }
        try {
            if (preparedStatement != null)
                preparedStatement.close();
        } catch (SQLException e) {
            LOGGER.error("Exception during disconnection from database", e);
        }
        try {
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Exception during disconnection from database", e);
        }
    }
}
