<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 25.05.2018
  Time: 23:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Edit object</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript">
        var customId = 1;
        var previousInput = null;
        function addLesson() {
            if (previousInput != null && previousInput.value === '') {
                alert("Fill in the previous field!");
            } else {
                var label = document.createElement("label");
                var p = document.createElement("p");
                var text = document.createTextNode("Lesson  ");
                var input = document.createElement("input");
                label.appendChild(text);
                input.setAttribute("name","9");
                input.setAttribute("type","text");
                input.setAttribute("required","");
                input.setAttribute("id",customId);
                customId+=1;
                label.appendChild(input);
                p.appendChild(label);
                document.getElementById("editForm").append(p);
                previousInput = input;
            }
        }
    </script>

    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
    </style>
</head>
<body>
<form method="POST" id="editForm">
    <%LWObject object = (LWObject) request.getAttribute("object"); %>
    <input type="submit" formaction="submitEdit" value="Save" >
    <input type="submit" formaction="back" value="Back">
    <% if (object.getObjectTypeID()==4) {%>
        <input type="button" value="Add lesson" onclick="addLesson()">
    <%}%>
    <p><label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObjectID()%>" class="lft" readonly>
    </label></p>
    <p><label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>" class="lft">
    </label></p>
    <p>Parameters:</p>
    <% Set<String> keySet = object.getParams().keySet();
        for (String key : keySet) {
            int id = Integer.parseInt(key.substring(0, key.indexOf("_")));
            String name = key.substring(key.indexOf("_") + 1, key.length());
            List<String> list = (List<String>) object.getParams().get(key);
            for (String temp : list) {%>
    <p>
        <label>
            <%=name%>
            <input type="text" name="<%=id%>" value="<%=temp%>" class="lft">
        </label>
    </p>
    <%} %>
    <%}%>

</form>
</body>
</html>
