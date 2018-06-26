package org.lab.three.connect;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

@Configuration
@PropertySource("classpath:jdbc.properties")
public class OracleConnect {
    private static final Logger LOGGER = Logger.getLogger(OracleConnect.class);
    private String datasourceName;
    private String wlsFactory;
    private String server;
    /**
     * connects to database
     */
    public Connection connect() {
        LOGGER.debug("Connecting to database");
        Connection connection = null;
        Hashtable hashtable = new Hashtable();
        LOGGER.debug(wlsFactory + " factory");
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, wlsFactory);
        hashtable.put(Context.PROVIDER_URL, server);
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

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setWlsFactory(String wlsFactory) {
        this.wlsFactory = wlsFactory;
    }
}
