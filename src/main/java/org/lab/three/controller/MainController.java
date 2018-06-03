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
    public static final String OPEN_TR = "<tr>";
    public static final String CLOSE_TR = "</tr>";
    public static final String OPEN_TH = "<th>";
    public static final String CLOSE_TH = "</th>";
    public static final String OPEN_TD = "<td>";
    public static final String CLOSE_TD = "</td>";
    public static final String LIST = "list";

    @Autowired
    private DAO dao;

    @RequestMapping(value={"/home", "/sign"})
    public ModelAndView showObjects() {
        LOGGER.debug("Showing top objects");
        List<LWObject> list = dao.getTopObject();
        return new ModelAndView("showAllObjects", LIST, list);
    }

    @RequestMapping("/children")
    public ModelAndView showChildren(@RequestParam(value = "object_id") String objectID) {
        LOGGER.debug("Showing children");
        int id = Integer.parseInt(objectID.substring(objectID.lastIndexOf("_") + 1, objectID.length()));
        List<LWObject> list = dao.getChildren(id);
        if (list.isEmpty()) {
            return new ModelAndView("showAllObjects", LIST, objectID);
        }
        return new ModelAndView("showAllObjects", LIST, list);
    }

    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = "object_id") String... arrays) {
        LOGGER.debug("Removing objects");
        int[] objectIdArray = new int[arrays.length];
        String parent_id = "0";
        for (int i = 0; i < arrays.length; i++) {
            objectIdArray[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf("_") + 1, arrays[i].length()));
            parent_id = arrays[i].substring(0, arrays[i].indexOf("_"));
        }
        List<LWObject> list = dao.removeByID(objectIdArray, parent_id);
        return new ModelAndView("showAllObjects", LIST, list);
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
        int objectID = dao.createObject(objectName, parentId, objectType);
        Map<Integer, String> attr = dao.getAttrByObjectIdFromAOT(Integer.parseInt(objectType));
        for (Map.Entry<Integer, String> temp : attr.entrySet()) {
            if (request.getParameter(Integer.toString(temp.getKey())) != null) {
                String value = request.getParameter(Integer.toString(temp.getKey()));
                dao.updateParams(objectID, temp.getKey(), value);
            }
        }
        List<LWObject> list;
        if ("0".equals(parentId)) {
            list = dao.getTopObject();
        } else {
            list = dao.getChildren(Integer.parseInt(parentId));
        }
        return new ModelAndView("showAllObjects", LIST, list);
    }

    @RequestMapping("/edit")
    public ModelAndView editObject(@RequestParam(value = "object_id") String objectId) {
        LOGGER.debug("Editing objects");
        int id = Integer.parseInt(objectId.substring(objectId.indexOf("_") + 1, objectId.length()));
        LWObject lwObject = dao.getObjectById(id);
        return new ModelAndView("editObject", "object", lwObject);
    }

    @RequestMapping("/submitEdit")
    public ModelAndView submitEdit(@RequestParam(value = "name") String name,
                                   @RequestParam(value = "objectId") int objectId,
                                   HttpServletRequest request) {
        LOGGER.debug("Submiting edit");
        ArrayList<Integer> attr = (ArrayList<Integer>) dao.getAttrByObjectIdFromParams(objectId);
        for (Integer temp : attr) {
            if (request.getParameter(Integer.toString(temp)) != null) {
                String value = request.getParameter(Integer.toString(temp));
                dao.updateParams(objectId, temp, value);
            }
        }
        List<LWObject> list = dao.changeNameById(objectId, name);
        return new ModelAndView("showAllObjects", LIST, list);
    }

    @RequestMapping(value = "/params", method = RequestMethod.GET)
    public @ResponseBody
    String params(@RequestParam(value = "ot") String objectType) {
        Map<Integer, String> map = dao.getAttrByObjectIdFromAOT(Integer.parseInt(objectType));
        StringBuilder code = new StringBuilder();
        for (Map.Entry<Integer, String> temp : map.entrySet()) {
            code.append("<p><label>");
            code.append(temp.getValue());
            code.append(": ");
            code.append("<input type=\"text\" name=\"");
            code.append(temp.getKey());
            code.append("\" value=\"\" required>");
            code.append("</label></p>");
        }
        return code.toString();
    }

    @RequestMapping("/info")
    public ModelAndView seeInfo(@RequestParam(value = "object_id") String objectID) {
        LOGGER.debug("See info objects");
        int id = Integer.parseInt(objectID.substring(objectID.indexOf("_") + 1, objectID.length()));
        LWObject LWObject = dao.getObjectById(id);
        return new ModelAndView("infoObject", "object", LWObject);
    }

    @RequestMapping("/back")
    public ModelAndView back(HttpServletRequest request) {
        LOGGER.debug("Back");
        String id;
        if (request.getParameter("objectId") == null) {
            id = request.getParameter("parentId");
        } else {
            id = request.getParameter("objectId");
        }
        List<LWObject> list = dao.getObjectsListByObject(Integer.parseInt(id));
        return new ModelAndView("showAllObjects", LIST, list);
    }


    @RequestMapping("/visit")
    public ModelAndView visitTable() {
        LOGGER.debug("Visit");
        Map<Integer, String> lessons = dao.getObjectsByObjectType(6);
        return new ModelAndView("visitPage", "lessons", lessons);
    }

    @RequestMapping(value = "/lesson", method = RequestMethod.GET)
    public @ResponseBody
    String getStudents(@RequestParam(value = "lesson") int lessonId) {
        LOGGER.debug("Lesson");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        StringBuilder code = new StringBuilder();
        Map<Integer, String> students = dao.getStudentsByLessonId(lessonId);
        List<Visit> list = dao.getVisitByLessonId(lessonId);
        List<String> dateList = dao.getDistinctDateByLessonId(lessonId);
        code.append("<table border='2' id='my-table'>").append("<tr><th>Name</th>");
        for (String aDateSet : dateList) {
            code.append("<td><div class='date'>");
            code.append(aDateSet);
            code.append("</div></td>");
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
        return new ModelAndView("searchObject", LIST, allObjectTypes);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public @ResponseBody
    String find(@RequestParam(value = "o") String name,
                @RequestParam(value = "ot") int typeID) {
        List<LWObject> list = dao.getLWObjectByNameAndType(name, typeID);
        StringBuilder code = new StringBuilder();
        if (list.isEmpty()) {
            code.append("No matches found");
        } else {
            code.append("<table border='2'>");
            code.append(OPEN_TR);
            code.append(OPEN_TH + " № " + CLOSE_TH);
            code.append(OPEN_TH + "ObjectID" + CLOSE_TH);
            code.append(OPEN_TH + "ParentID" + CLOSE_TH);
            code.append(OPEN_TH + "Name" + CLOSE_TH);
            code.append(OPEN_TH + "ObjectTypeID" + CLOSE_TH);
            code.append(CLOSE_TR);
            for (LWObject lwObject : list) {
                code.append(OPEN_TR);
                code.append(OPEN_TD + "<input id='object_id' type='checkbox' name='object_id' value='").append(lwObject.getParentID()).append("_").append(lwObject.getObjectID()).append("'>").append(CLOSE_TD);
                code.append(OPEN_TD + lwObject.getObjectID() + CLOSE_TD);
                code.append(OPEN_TD + lwObject.getParentID() + CLOSE_TD);
                code.append(OPEN_TD + lwObject.getName() + CLOSE_TD);
                code.append(OPEN_TD + lwObject.getObjectTypeID() + CLOSE_TD);
                code.append(CLOSE_TR);
            }
            code.append("</table>");
        }
        return code.toString();
    }


}