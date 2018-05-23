package org.lab.three.controller;

import org.lab.three.beans.lwObject;
import org.lab.three.dao.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.List;

@Controller
public class MainController {
    @Autowired
    private DAO dao;

    @RequestMapping("/sign")
    public ModelAndView showObjects() {
        List<lwObject> list = dao.getTopObject();
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/children")
    public ModelAndView showChildren(@RequestParam(value = "object_id") int object_id) {
        List<lwObject> list = dao.getChildren(object_id);
        return new ModelAndView("showAllObjects", "list", list);
    }
}
