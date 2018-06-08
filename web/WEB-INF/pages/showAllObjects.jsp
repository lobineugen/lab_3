<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.ArrayList" %>
<%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 21.05.2018
  Time: 23:15
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Show all object</title>
    <script src="http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.6.1.min.js" type="text/javascript"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.30.4/js/jquery.tablesorter.min.js"
            type="text/javascript"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.30.4/js/extras/jquery.tablesorter.pager.min.js"
            type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function(){
            if($("#parentId").val() == 0 ){
                $("#hidden").hide();
            }
            $("#object_list").tablesorter({sortList: [[0, 1]]});
        });

        function show_alert() {
            var int = checkBoxChecked();
            if (int > 0) {
                if (confirm("Do you really want to removing this objects?"))
                    document.forms[0].submit();
                else
                    return false;
            } else {
                alert("Select one or more objects for removing!");
                return false;
            }

        }

        function edit_check() {
            if(checkBoxChecked() !== 1) {
                alert("Select only one object for editing!");
                return false;
            }
            return true;
        }


        function info_check() {
            if(checkBoxChecked() !== 1) {
                alert("Select only one object for see info!");
                return false;
            }
            return true;
        }


        function checkBoxChecked() {
            return $("input[name='object_id']:checked").length;
        }

        function getPath() {
            $.ajax({
                url : 'path',
                data : 'objectId=' + document.getElementById("parentId").value,
                success : function(data) {
                    var newArr = data.split(";");
                    for( var i = 0; i <newArr.length-1; i++) {
                        var x = newArr[i].toString().split(":");
                        $('#topPath').after($('<a>', {
                            href: "cPath?object_id=" + x[0],
                            text: x[1] + "/"
                        }));
                    }
                }
            });
        }
    </script>
    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
    </style>
</head>
<body onload="getPath()">
<form method="post">

    <table border="2" id="object_list">
        <thead>
        <tr>
            <th>№</th>
            <th><a>Icon</a></th>
            <th><a>Name</a></th>
        </tr>
        </thead>
        <tbody>
        <% if (request.getAttribute("list") instanceof ArrayList) {%>
        <c:forEach var="objects" items="${list}">
            <tr>
                <td><input id="object_id" type="checkbox" name="object_id"
                           value="${objects.parentID}_${objects.objectID}"></td>
                <td>
                    <c:choose>
                        <c:when test="${objects.objectTypeID=='1'}">
                            <img src="<c:url value="/resources/images/univer.png" />" class="icon"/>
                        </c:when>
                        <c:when test="${objects.objectTypeID=='2'}">
                            <img src="<c:url value="/resources/images/group.png" />" class="icon"/>
                        </c:when>
                        <c:when test="${objects.objectTypeID=='3'}">
                            <img src="<c:url value="/resources/images/department.png" />" class="icon"/>
                        </c:when>
                        <c:when test="${objects.objectTypeID=='4'}">
                            <img src="<c:url value="/resources/images/stud.png" />" class="icon"/>
                        </c:when>
                        <c:when test="${objects.objectTypeID=='5'}">
                            <img src="<c:url value="/resources/images/teacher.png" />" class="icon"/>
                        </c:when>
                        <c:when test="${objects.objectTypeID=='6'}">
                            <img src="<c:url value="/resources/images/lesson.png" />" class="icon"/>
                        </c:when>
                    </c:choose>
                </td>
                <c:choose>
                    <c:when test="${objects.objectTypeID=='4'}">
                        <td>${objects.name}</td>
                    </c:when>
                    <c:when test="${objects.objectTypeID=='5'}">
                        <td>${objects.name}</td>
                    </c:when>
                    <c:when test="${objects.objectTypeID=='6'}">
                        <td>${objects.name}</td>
                    </c:when>
                    <c:otherwise>
                        <td><a href="children?object_id=${objects.objectID}">${objects.name}</a></td>
                    </c:otherwise>
                </c:choose>

            </tr>
        </c:forEach>
        </tbody>

            <%ArrayList list = ((ArrayList) request.getAttribute("list")); %>
            <% if (list.size() > 0) {%>
            <input type="hidden" name="parentId" value="<%=((LWObject)list.get(0)).getParentID()%>" id="parentId"/>
            <% } else { %>
            <input type="hidden" name="parentId" value="0" id="parentId"/>
            <%} %>
        <%} else {%>

        <input type="hidden" name="parentId" value="<%=request.getAttribute("list")%>"/>
        <%} %>
        <%if (session.getAttribute("right").equals("FULL")) {%>
            <input type="submit" formaction="add" value="Add">
            <input type="submit" formaction="remove" value="Remove" onclick="return show_alert()">
            <input type="submit" formaction="edit" value="Edit" onclick="return edit_check()">
        <%} else if(session.getAttribute("right").equals("EDIT")) {%>
            <input type="submit" formaction="edit" value="Edit" onclick="return edit_check()">
        <%} %>
        <input type="submit" formaction="info" value="Info" onclick="return info_check()">
        <input type="submit" formaction="back" value="Previous" id="hidden">
        <input type="submit" formaction="search" value="Search">

        <input type="submit" formaction="visit" value="Visit" id="hidden"><br/>
        Path: <a href="top" id="topPath">Top/</a>
    </table>
</form>


</body>
</html>
