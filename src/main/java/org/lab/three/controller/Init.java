package org.lab.three.controller;

import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

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
//        LOGGER.debug("Initializing context");
//        ApplicationContext applicationContext = (ApplicationContext)servletContextEvent.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
//        DAO dao = applicationContext.getBean(DAOOracleImpl.class);
//        Connection connection;
//        OracleConnect oracleConnect = applicationContext.getBean(OracleConnect.class);
//        connection = oracleConnect.connect();
//        dao.executeScript(connection);
//        oracleConnect.disconnect(connection,null,null);
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
