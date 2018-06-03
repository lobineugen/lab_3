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
    public static final String OBJ_ID = "objectId";
    public static final String LIST = "list";
    public static final String SHOW_ALL_OBJECTS = "showAllObjects";
    public static final String OBJECT_ID = "object_id";

    @Autowired
    private DAO dao;

    @RequestMapping(value = {"/home", "/sign"})
    public ModelAndView showObjects(HttpServletRequest request) {
        LOGGER.debug("Showing top objects");
        List<LWObject> list = dao.getTopObject();
        String userName = request.getParameter("userName");
        request.getSession().setAttribute("right", dao.getRightByUserName(userName));
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
    }

    @RequestMapping("/children")
    public ModelAndView showChildren(@RequestParam(value = OBJECT_ID) String objectID) {
        LOGGER.debug("Showing children");
        int id = Integer.parseInt(objectID.substring(objectID.lastIndexOf('_') + 1, objectID.length()));
        List<LWObject> list = dao.getChildren(id);
        if (list.isEmpty()) {
            return new ModelAndView(SHOW_ALL_OBJECTS, LIST, objectID);
        }
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
    }

    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = OBJECT_ID) String... arrays) {
        LOGGER.debug("Removing objects");
        int[] objectIdArray = new int[arrays.length];
        String parentID = "0";
        for (int i = 0; i < arrays.length; i++) {
            objectIdArray[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf('_') + 1, arrays[i].length()));
            parentID = arrays[i].substring(0, arrays[i].indexOf('_'));
        }
        List<LWObject> list = dao.removeByID(objectIdArray, parentID);
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
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
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
    }

    @RequestMapping("/edit")
    public ModelAndView editObject(@RequestParam(value = OBJECT_ID) String objectId) {
        LOGGER.debug("Editing objects");
        int id = Integer.parseInt(objectId.substring(objectId.indexOf('_') + 1, objectId.length()));
        LWObject lwObject = dao.getObjectById(id);
        return new ModelAndView("editObject", "object", lwObject);
    }

    @RequestMapping("/submitEdit")
    public ModelAndView submitEdit(@RequestParam(value = "name") String name,
                                   @RequestParam(value = OBJ_ID) int objectId,
                                   HttpServletRequest request) {
        LOGGER.debug("Submiting edit");
        ArrayList<Integer> attr = (ArrayList<Integer>) dao.getAttrByObjectIdFromParams(objectId);
        Map<String, String[]> params = request.getParameterMap();
        Set<String> keySet = params.keySet();
        for (int temp : attr) {
            for (String key : keySet) {
                if (!("name").equals(key) && !("objectId").equals(key)) {
                    if (Integer.parseInt(key) == temp && params.get(key) != null) {
                        String[] value = params.get(key);
                        for (String par : value) {
                            dao.updateParams(objectId,temp,par);
                        }
                    }
                }

            }
        }
        List<LWObject> list = dao.changeNameById(objectId, name);
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
    }

    @RequestMapping(value = "/params", method = RequestMethod.GET)
    public @ResponseBody
    String params(@RequestParam(value = "ot") String objectType) {
        Map<Integer, String> map = dao.getAttrByObjectIdFromAOT(Integer.parseInt(objectType));
        StringBuilder code = new StringBuilder(75);
        for (Map.Entry<Integer, String> temp : map.entrySet()) {
            code.append("<p><label>");
            code.append(temp.getValue());
            code.append(": <input type=\"text\" name=\"");
            code.append(temp.getKey());
            code.append("\" value=\"\" required></label></p>");
        }
        return code.toString();
    }

    @RequestMapping("/info")
    public ModelAndView seeInfo(@RequestParam(value = OBJECT_ID) String objectID) {
        LOGGER.debug("See info objects");
        int id = Integer.parseInt(objectID.substring(objectID.indexOf('_') + 1, objectID.length()));
        LWObject lwObject = dao.getObjectById(id);
        return new ModelAndView("infoObject", "object", lwObject);
    }

    @RequestMapping("/back")
    public ModelAndView back(HttpServletRequest request) {
        LOGGER.debug("Back");
        String id;
        if (request.getParameter(OBJ_ID) == null) {
            id = request.getParameter("parentId");
        } else {
            id = request.getParameter(OBJ_ID);
        }
        List<LWObject> list = dao.getObjectsListByObject(Integer.parseInt(id));
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
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
        StringBuilder code = new StringBuilder(90);
        Map<Integer, String> students = dao.getStudentsByLessonId(lessonId);
        List<Visit> list = dao.getVisitByLessonId(lessonId);
        List<String> dateList = dao.getDistinctDateByLessonId(lessonId);
        code.append("<table border='2' id='my-table'><tr><th>Name</th>");
        for (String aDateSet : dateList) {
            code.append("<td><div class='date'>");
            code.append(aDateSet);
            code.append("</div></td>");
        }
        code.append(CLOSE_TR);
        if (students.isEmpty()) {
            code.append("Students not found for this lesson");
        } else {
            int numb = 1;
            int count = 0;
            for (Map.Entry<Integer, String> map : students.entrySet()) {
                code.append(OPEN_TR + OPEN_TD);
                code.append("<input id='object");
                code.append(numb++);
                code.append("' type='hidden' name='objectId' value='");
                code.append(map.getKey());
                code.append("'>");
                code.append(map.getValue());
                code.append(CLOSE_TD);
                for (String date : dateList) {
                    for (Visit visit : list) {
                        if (visit.getDate().equals(date) && visit.getObjectId() == map.getKey()) {
                            code.append(OPEN_TD);
                            code.append("<input type='text' name='");
                            code.append(map.getKey());
                            code.append('_');
                            code.append(date);
                            code.append("' value='");
                            code.append(visit.getMark());
                            code.append("'>");
                            code.append(CLOSE_TD);
                            count = 0;
                            break;
                        } else {
                            count = 1;
                        }
                    }
                    if (count == 1) {
                        code.append(OPEN_TD);
                        code.append("<input type='text' name='");
                        code.append(map.getKey());
                        code.append('_');
                        code.append(date);
                        code.append("' value='-' readonly>");
                        code.append(CLOSE_TD);
                        count = 0;
                    }
                }

                code.append(CLOSE_TR);
            }
        }
        code.append("</table>");
        return code.toString();
    }

    @RequestMapping("/saveVisit")
    public ModelAndView saveVisit(HttpServletRequest request) {
        LOGGER.debug("Save visit");
        Map<String, String[]> map = request.getParameterMap();
        String[] lessonId = map.get("lessons");
        String[] objectIds = map.get(OBJ_ID);
        Set<String> keySet = map.keySet();
        for (String id : objectIds) {
            for (Object aKeySet : keySet) {
                String key = aKeySet.toString();
                if (key.startsWith(id + '_')) {
                    dao.insertVisit(lessonId[0], id, key.substring(key.indexOf('_') + 1, key.length()),
                            map.get(key)[0]);
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
            code.append(OPEN_TH + "N" + CLOSE_TH);
            code.append(OPEN_TH + "ObjectID" + CLOSE_TH);
            code.append(OPEN_TH + "ParentID" + CLOSE_TH);
            code.append(OPEN_TH + "Name" + CLOSE_TH);
            code.append(OPEN_TH + "ObjectTypeID" + CLOSE_TH);
            code.append(CLOSE_TR);
            for (LWObject lwObject : list) {
                code.append(OPEN_TR);
                code.append(OPEN_TD + "<input id='object_id' type='checkbox' name='object_id'" +
                        " value='" + lwObject.getParentID() + '_' + lwObject.getObjectID() + "'>" + CLOSE_TD);
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