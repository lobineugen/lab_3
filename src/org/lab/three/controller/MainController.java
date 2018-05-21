package org.lab.three.controller;

import org.lab.three.beans.lwObject;
import org.lab.three.dao.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class MainController {
    @Autowired
    private DAO dao;

    @RequestMapping("/sign")
    public ModelAndView showObjects() {
        List<lwObject> list = dao.getObjects();
        return new ModelAndView("showAllObjects", "list", list);
    }
}
