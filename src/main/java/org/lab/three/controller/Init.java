package org.lab.three.controller;

import com.ibatis.common.jdbc.ScriptRunner;
import org.lab.three.dao.DAO;
import org.lab.three.dao.DAOOracleImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.URL;

public class Init implements ServletContextListener {
    private DAO dao = new DAOOracleImpl();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        int count = dao.checkTables();
        if (count<5) {
            dao.executeScript();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
