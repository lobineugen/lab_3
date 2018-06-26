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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MainController class connects view and dao
 */
@Controller
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class);
    private static final String OBJ_ID = "objectId";
    private static final String LIST = "list";
    private static final String SHOW_ALL_OBJECTS = "showAllObjects";
    private static final String OBJECT_ID = "object_id";

    @Autowired
    private DAO dao;

    /**
     * Showing top objects considering entered role
     *
     * @param request
     */
    @RequestMapping(value = {"/sign"})
    public ModelAndView showObjects(HttpServletRequest request) {
        LOGGER.debug("Showing top objects considering role");
        request.getSession().setAttribute("right", dao.getRightByUserName(request.getParameter("userName")));
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, dao.getTopObject());
    }


    /**
     * Shows top objects
     */
    @RequestMapping(value = {"/home", "/top"})
    public ModelAndView getTop() {
        LOGGER.debug("Showing top objects");
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, dao.getTopObject());
    }

    /**
     * Shows children objects
     *
     * @param objectID
     */
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

    /**
     * Gets children path
     *
     * @param objectID
     */
    @RequestMapping("/cPath")
    public ModelAndView getChildrenPath(@RequestParam(value = OBJECT_ID) int objectID) {
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, dao.getParentByChildren(objectID));
    }

    /**
     * Shows all objects page after removing object
     *
     * @param arrays
     */
    @RequestMapping("/remove")
    public ModelAndView removeObject(@RequestParam(value = OBJECT_ID) String... arrays) {
        LOGGER.debug("Removing objects");
        int[] objectIdArray = new int[arrays.length];
        String parentID = "0";
        for (int i = 0; i < arrays.length; i++) {
            objectIdArray[i] = Integer.parseInt(arrays[i].substring(arrays[i].indexOf('_') + 1, arrays[i].length()));
            parentID = arrays[i].substring(0, arrays[i].indexOf('_'));
        }
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, dao.removeByID(objectIdArray, parentID));
    }

    /**
     * Shows add object page
     *
     * @param parentId
     */
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

    /**
     * Shows all objects page with new object
     *
     * @param objectName
     * @param parentId
     * @param objectType
     */
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

    /**
     * Shows edit object page
     *
     * @param objectId
     */
    @RequestMapping("/edit")
    public ModelAndView editObject(@RequestParam(value = OBJECT_ID) String objectId) {
        LOGGER.debug("Editing objects");
        int id = Integer.parseInt(objectId.substring(objectId.indexOf('_') + 1, objectId.length()));
        LWObject lwObject = dao.getObjectById(id);
        return new ModelAndView("editObject", "object", lwObject);
    }

    /**
     * Shows all objects page and submits object editing
     *
     * @param name
     * @param objectId
     * @param request
     */
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
                if (temp == 9 && "9".equals(key)) {
                    if (params.get(key).length == 1 && params.get(key)[0].equals("0")) {
                        dao.deleteAllLessons(objectId);
                    } else {
                        dao.deleteAllLessons(objectId);
                        for (String less : params.get(key)) {
                            dao.updateLessons(objectId, less);
                        }
                    }
                } else {
                    if ((!("name").equals(key) && !("objectId").equals(key)) && (Integer.parseInt(key) == temp &&
                            params.get(key) != null)) {
                            String[] value = params.get(key);
                            for (String par : value) {
                                dao.updateParams(objectId, temp, par);
                            }
                    }
                }
            }
        }
        List<LWObject> list = dao.changeNameById(objectId, name);
        return new ModelAndView(SHOW_ALL_OBJECTS, LIST, list);
    }

    /**
     * Shows object parameters
     *
     * @param objectType
     */
    @RequestMapping(value="/params", method=RequestMethod.POST,
            produces="application/json")
    public @ResponseBody
    Map<Integer, String> params(@RequestParam(value = "ot") String objectType) {
        return dao.getAttrByObjectIdFromAOT(Integer.parseInt(objectType));
    }

    /**
     * Shows page with all information about object
     *
     * @param objectID
     */
    @RequestMapping("/info")
    public ModelAndView seeInfo(@RequestParam(value = OBJECT_ID) String objectID) {
        LOGGER.debug("See info objects");
        int id = Integer.parseInt(objectID.substring(objectID.indexOf('_') + 1, objectID.length()));
        LWObject lwObject = dao.getObjectById(id);
        return new ModelAndView("infoObject", "object", lwObject);
    }

    /**
     * Returns to objects page
     *
     * @param request
     */
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

    /**
     * Shows visit page
     *
     */
    @RequestMapping("/visit")
    public ModelAndView visitTable() {
        LOGGER.debug("Visit");
        Map<Integer, String> lessons = dao.getObjectsByObjectType(6);
        return new ModelAndView("visitPage", "lessons", lessons);
    }



    /**
     * Shows lesson conducting dates
     *
     * @param lessonId
     */
    @RequestMapping(value="/lesson", method=RequestMethod.POST,
            produces="application/json")
    public @ResponseBody
    Map<Integer, String>  getLessons(@RequestParam(value = "lesson") int lessonId) {
        LOGGER.debug("Lesson");
        return dao.getStudentsByLessonId(lessonId);
    }

    @RequestMapping(value="/lesson_visits", method=RequestMethod.POST,
            produces="application/json")
    public @ResponseBody
    List<Visit>  getVisits(@RequestParam(value = "lesson") int lessonId) {
        LOGGER.debug("Lesson");
        return dao.getVisitByLessonId(lessonId);
    }

    @RequestMapping(value="/lesson_date", method=RequestMethod.POST,
            produces="application/json")
    public @ResponseBody
    List<String>  getDate(@RequestParam(value = "lesson") int lessonId) {
        LOGGER.debug("Lesson");
        return dao.getDistinctDateByLessonId(lessonId);
    }


    /**
     * Saves new dates of lesson conducting and shows visit page
     *
     * @param request
     */
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

    /**
     * Shows search objects page
     *
     */
    @RequestMapping("/search")
    public ModelAndView searchObject() {
        LOGGER.debug("Serching new objects");
        Map<Integer, String> allObjectTypes = dao.getAllObjectTypes();
        return new ModelAndView("searchObject", LIST, allObjectTypes);
    }

    /**
     * Shows searching results at search objects page
     *
     * @param name
     * @param typeID
     */
    @RequestMapping(value="/find", method=RequestMethod.POST,
            produces="application/json")
    public @ResponseBody
    List<LWObject> find(@RequestParam(value = "o") String name,
                @RequestParam(value = "ot") int typeID ) {
        return dao.getLWObjectByNameAndType(name, typeID);
    }

    /**
     * returns student lessons names
     *
     * @param arrayId
     */
    @RequestMapping(value = "/lessonsName", method = RequestMethod.GET)
    public @ResponseBody
    String getLessonsName(@RequestParam(value = "lessonsId") String arrayId) {
        StringBuilder arrays = new StringBuilder();
        String[] arrayIds = arrayId.split("/");
        for (String str : arrayIds) {
            arrays.append(str);
            arrays.append(':');
            arrays.append(dao.getNameById(Integer.parseInt(str)));
            arrays.append(';');
        }
        return arrays.toString();
    }

    /**
     * Gets all lessons names
     */
    @RequestMapping(value = "/allLessons", method = RequestMethod.GET)
    public @ResponseBody
    String getAllLessons() {
        StringBuilder allLessons = new StringBuilder();
        Map<Integer, String> map = dao.getObjectsByObjectType(6);
        for (Map.Entry<Integer, String> temp : map.entrySet()) {
            allLessons.append(temp.getKey()).append(':').append(temp.getValue()).append(';');
        }
        return allLessons.toString();
    }

    /**
     * Gets path to object in objects hierarchy
     *
     * @param objectId
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    public @ResponseBody
    String getPath(@RequestParam(value = "objectId") int objectId) {
        StringBuilder path = new StringBuilder();
        Map<Integer, String> map = dao.getPath(objectId);
        for (Map.Entry<Integer, String> temp : map.entrySet()) {
            path.append(temp.getKey()).append(':').append(temp.getValue()).append(';');
        }
        return path.toString();
    }
}