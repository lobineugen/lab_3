package org.lab.three.controller;

import org.apache.log4j.Logger;
import org.lab.three.dao.DAO;
import org.lab.three.dao.DAOOracleImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Init implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(Init.class);
    private DAO dao = new DAOOracleImpl();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.debug("Initializing context");
        int count = dao.checkTables();
        if (count<5) {
            dao.executeScript();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
