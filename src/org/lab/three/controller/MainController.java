package org.lab.three.controller;

import org.lab.three.beans.lwObject;
import org.lab.three.dao.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        int id = Integer.parseInt(object_id.substring(object_id.lastIndexOf("_") + 1, object_id.length()));
        List<lwObject> list = dao.getChildren(id);
        if (list.size() == 0) {
            return new ModelAndView("showAllObjects", "list", object_id);
        }
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = "object_id") String... arrays) {
        int[] object_id_array = new int[arrays.length];
        String parent_id = "0";
        for (int i = 0; i < arrays.length; i++) {
            object_id_array[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf("_") + 1, arrays[i].length()));
            parent_id = arrays[i].substring(0, arrays[i].indexOf("_"));
        }
        List<lwObject> list = dao.removeByID(object_id_array, parent_id);
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/add")
    public ModelAndView addNewObject(@RequestParam(value = "parentId") int parentId) {
        Map<Integer, String> objectTypes = dao.getObjectTypes(parentId);
        ArrayList<String> array = new ArrayList<>();
        array.add(String.valueOf(parentId));
        for (Map.Entry<Integer, String> entry : objectTypes.entrySet()) {
            array.add(String.valueOf(entry.getKey()));
            array.add(entry.getValue());
        }
        return new ModelAndView("addObject", "array", array);
    }

    @RequestMapping("/create")
    public ModelAndView createNewObject(@RequestParam(value = "objectName") String objectName,
                                        @RequestParam(value = "parentId") String parentId,
                                        @RequestParam(value = "objectType") String objectType) {
        dao.createObject(objectName, parentId, objectType);
        List<lwObject> list;
        if (parentId.equals("0")) {
            list = dao.getTopObject();
        } else {
            list = dao.getChildren(Integer.parseInt(parentId));
        }
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/edit")
    public ModelAndView editObject(@RequestParam(value = "object_id") String objectId) {
        int id = Integer.parseInt(objectId.substring(objectId.indexOf("_") + 1, objectId.length()));
        lwObject lwObject = dao.getObjectById(id);
        return new ModelAndView("editObject", "object", lwObject);
    }

    @RequestMapping("/submitEdit")
    public ModelAndView submitEdit(@RequestParam(value = "name") String name,
                                   @RequestParam(value = "objectId") int objectId) {
        List<lwObject> list = dao.changeNameById(objectId, name);
        return new ModelAndView("showAllObjects", "list", list);
    }
}
