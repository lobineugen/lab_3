package org.lab.three.controller;

import org.apache.log4j.Logger;
import org.lab.three.connect.OracleConnect;
import org.lab.three.dao.DAO;
import org.lab.three.dao.DAOOracleImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

/**
 * Init class checks necessity of script running
 */
@WebListener
public class Init implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(Init.class);

    /**
     * Runs SQL tables creation script under certain conditions
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.debug("Initializing context");
        ApplicationContext applicationContext = (ApplicationContext)servletContextEvent.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        DAO dao = applicationContext.getBean(DAOOracleImpl.class);
        Connection connection;
        OracleConnect oracleConnect = applicationContext.getBean(OracleConnect.class);
        connection = oracleConnect.connect();
        dao.executeScript(connection);
        oracleConnect.disconnect(connection,null,null);
    }

    /**
     * Destroying context
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.debug("Destroying context");
    }
}
