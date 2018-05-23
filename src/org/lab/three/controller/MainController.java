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
    public ModelAndView showChildren(@RequestParam(value = "object_id") String object_id) {
        int id  = Integer.parseInt(object_id.substring(object_id.lastIndexOf("_")+1 , object_id.length()));
        List<lwObject> list = dao.getChildren(id);
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = "object_id") String... arrays) {
        System.out.println(arrays.length + " размер массива для удаления");
        int[] object_id_array= new int[arrays.length];
        int parent_id = 0;
        for (int i = 0; i <arrays.length; i++){
            object_id_array[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf("_")+1 , arrays[i].length()));
            System.out.println("object_id: " + object_id_array[i]);
            parent_id = Integer.parseInt(arrays[i].substring(0,arrays[i].indexOf("_")));
            System.out.println("parent id: " + parent_id);
        }
        List<lwObject> list = dao.removeByID(object_id_array, parent_id);
        return new ModelAndView("showAllObjects", "list", list);
    }
}
