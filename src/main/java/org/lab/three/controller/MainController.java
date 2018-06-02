package org.lab.three.controller;

import org.apache.log4j.Logger;
import org.lab.three.beans.LWObject;
import org.lab.three.beans.Visit;
import org.lab.three.dao.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class);

    @Autowired
    private DAO dao;

    @RequestMapping(value={"/home", "/sign"})
    public ModelAndView showObjects() {
        LOGGER.debug("Showing top objects");
        List<LWObject> list = dao.getTopObject();
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/children")
    public ModelAndView showChildren(@RequestParam(value = "object_id") String object_id) {
        LOGGER.debug("Showing children");
        int id = Integer.parseInt(object_id.substring(object_id.lastIndexOf("_") + 1, object_id.length()));
        List<LWObject> list = dao.getChildren(id);
        if (list.size() == 0) {
            return new ModelAndView("showAllObjects", "list", object_id);
        }
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = "object_id") String... arrays) {
        LOGGER.debug("Removing objects");
        int[] object_id_array = new int[arrays.length];
        String parent_id = "0";
        for (int i = 0; i < arrays.length; i++) {
            object_id_array[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf("_") + 1, arrays[i].length()));
            parent_id = arrays[i].substring(0, arrays[i].indexOf("_"));
        }
        List<LWObject> list = dao.removeByID(object_id_array, parent_id);
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/add")
    public ModelAndView addNewObject(@RequestParam(value = "parentId") int parentId) {
        LOGGER.debug("Adding new objects");
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
                                        @RequestParam(value = "objectType") String objectType,
                                        HttpServletRequest request) {
        LOGGER.debug("Creating new objects");
        int object_id = dao.createObject(objectName, parentId, objectType);
        Map<Integer, String> attr = dao.getAttrByObjectIdFromAOT(Integer.parseInt(objectType));
        for (Map.Entry<Integer, String> temp : attr.entrySet()) {
            if (request.getParameter(Integer.toString(temp.getKey())) != null) {
                String value = request.getParameter(Integer.toString(temp.getKey()));
                dao.updateParams(object_id, temp.getKey(), value);
            }
        }
        List<LWObject> list;
        if (parentId.equals("0")) {
            list = dao.getTopObject();
        } else {
            list = dao.getChildren(Integer.parseInt(parentId));
        }
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping("/edit")
    public ModelAndView editObject(@RequestParam(value = "object_id") String objectId) {
        LOGGER.debug("Editing objects");
        int id = Integer.parseInt(objectId.substring(objectId.indexOf("_") + 1, objectId.length()));
        LWObject LWObject = dao.getObjectById(id);
        return new ModelAndView("editObject", "object", LWObject);
    }

    @RequestMapping("/submitEdit")
    public ModelAndView submitEdit(@RequestParam(value = "name") String name,
                                   @RequestParam(value = "objectId") int objectId,
                                   HttpServletRequest request) {
        LOGGER.debug("Submiting edit");
        ArrayList<Integer> attr = dao.getAttrByObjectIdFromParams(objectId);
        for (Integer temp : attr) {
            if (request.getParameter(Integer.toString(temp)) != null) {
                String value = request.getParameter(Integer.toString(temp));
                dao.updateParams(objectId, temp, value);
            }
        }
        List<LWObject> list = dao.changeNameById(objectId, name);
        return new ModelAndView("showAllObjects", "list", list);
    }

    @RequestMapping(value = "/params", method = RequestMethod.GET)
    public @ResponseBody
    String params(@RequestParam(value = "ot") String ot) {
        Map<Integer, String> map = dao.getAttrByObjectIdFromAOT(Integer.parseInt(ot));
        StringBuilder code = new StringBuilder();
        for (Map.Entry<Integer, String> temp : map.entrySet()) {
            code.append("<p><label>").append(temp.getValue()).append(": ");
            code.append("<input type=\"text\" name=\"").append(temp.getKey()).append("\" value=\"\" required>");
            code.append("</label></p>");
        }
        return code.toString();
    }

    @RequestMapping("/info")
    public ModelAndView seeInfo(@RequestParam(value = "object_id") String objectId) {
        LOGGER.debug("See info objects");
        int id = Integer.parseInt(objectId.substring(objectId.indexOf("_") + 1, objectId.length()));
        LWObject LWObject = dao.getObjectById(id);
        return new ModelAndView("infoObject", "object", LWObject);
    }

    @RequestMapping("/back")
    public ModelAndView back(HttpServletRequest request) {
        LOGGER.debug("Back");
        String id;
        if (request.getParameter("objectId") != null) {
            id = request.getParameter("objectId");
        } else {
            id = request.getParameter("parentId");
        }
        List<LWObject> list = dao.getObjectsListByObject(Integer.parseInt(id));
        return new ModelAndView("showAllObjects", "list", list);
    }


    @RequestMapping("/visit")
    public ModelAndView visitTable() {
        LOGGER.debug("Visit");
        Map<Integer, String> lessons = dao.getObjectsByObjectType(6);
        return new ModelAndView("visitPage", "lessons", lessons);
    }

    @RequestMapping(value = "/lesson", method = RequestMethod.GET)
    public @ResponseBody
    String getStudents(@RequestParam(value = "lesson") int lessonId) throws ParseException {
        LOGGER.debug("Lesson");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        StringBuilder code = new StringBuilder();
        Map<Integer, String> students = dao.getStudentsByLessonId(lessonId);
        List<Visit> list = dao.getVisitByLessonId(lessonId);
        List<String> dateList = dao.getDistinctDateByLessonId(lessonId);
        code.append("<table border='2' id='my-table'>").append("<tr><th>Name</th>");
        for (String aDateSet : dateList) {
            code.append("<td>");
            code.append(aDateSet);
            code.append("</td>");
        }
        code.append("</tr>");
        if (students.size() > 0) {
            int i = 1;
            int count = 0;
            for (Map.Entry<Integer, String> map : students.entrySet()) {
                code.append("<tr><td>");
                code.append("<input id='object").append(i++).append("' type='hidden' name='objectId' value='").append(map.getKey()).append("'>");
                code.append(map.getValue());
                code.append("</td>");
                for (String date : dateList) {
                    for (Visit visit : list) {
                        if (visit.getDate().equals(date) && visit.getObjectId() == map.getKey()) {
                            code.append("<td>");
                            code.append("<input type='text' name='").append(map.getKey()).append("_").append(date).append("' value='").append(visit.getMark()).append("'>");
                            code.append("</td>");
                            count = 0;
                            break;
                        } else {
                            count = 1;
                        }
                    }
                    if (count == 1) {
                        code.append("<td>");
                        code.append("<input type='text' name='").append(map.getKey()).append("_").append(date).append("' value='").append("-").append("' readonly>");
                        code.append("</td>");
                        count = 0;
                    }
                }

                code.append("</tr>");
            }
        } else {
            code.append("Students not found for this lesson");
        }
        code.append("</table>");
        return code.toString();
    }

    @RequestMapping("/saveVisit")
    public ModelAndView saveVisit(HttpServletRequest request) {
        LOGGER.debug("Save visit");
        Map<String, String[]> map = request.getParameterMap();
        String[] lessonId = map.get("lessons");
        String[] objectIds = map.get("objectId");
        Set<String> keySet = map.keySet();
        for (String id : objectIds) {
            for (Object aKeySet : keySet) {
                String key = aKeySet.toString();
                if (key.startsWith(id + "_")) {
                    dao.insertVisit(lessonId[0], id, key.substring(key.indexOf("_") + 1, key.length()), map.get(key)[0]);
                }
            }
        }
        Map<Integer, String> lessons = dao.getObjectsByObjectType(6);
        return new ModelAndView("visitPage", "lessons", lessons);
    }


    @RequestMapping("/search")
    public ModelAndView searchObject() {
        LOGGER.debug("Serching new objects");
        Map<Integer, String> allObjectTypes = dao.getAllObjectTypes();
        return new ModelAndView("searchObject", "array", allObjectTypes);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public @ResponseBody
    String find(@RequestParam(value = "o") String name,
                @RequestParam(value = "ot") int typeID) {
        List<LWObject> list = dao.getLWObjectByNameAndType(name, typeID);
        StringBuilder code = new StringBuilder();
        if (!list.isEmpty()) {
            code.append("<table border='2'>");
            code.append("<tr>");
            code.append("<th>№</th>");
            code.append("<th>ObjectID</th>");
            code.append("<th>ParentID</th>");
            code.append("<th>Name</th>");
            code.append("<th>ObjectTypeID</th>");
            code.append("</tr>");
            for (LWObject lwObject : list) {
                code.append("<tr>");
                code.append("<td><input id='object_id' type='checkbox' name='object_id'" +
                        " value='" + lwObject.getParent_id() + "_" + lwObject.getObject_id() + "'></td>");
                code.append("<td>" + lwObject.getObject_id() + "</td>");
                code.append("<td>" + lwObject.getParent_id() + "</td>");
                code.append("<td>" + lwObject.getName() + "</td>");
                code.append("<td>" + lwObject.getObject_type_id() + "</td>");
                code.append("</tr>");
            }
            code.append("</table>");
        } else {
            code.append("No matches found");
        }
        return code.toString();
    }


}