<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 31.05.2018
  Time: 22:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Object info</title>
    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
    </style>
</head>
<body>
<form method="POST">
    <%LWObject object = (LWObject) request.getAttribute("object"); %>
    <p><label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObjectID()%>" class="lft" readonly>
    </label></p>
    <p><label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>" class="lft" readonly>
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
                <input type="text" name="<%=id%>" value="<%=temp%>" class="lft" readonly>
                </label>
            </p>
            <%} %>
    <%}%>

    <input type="submit" formaction="back" value="Back">
</form>

</body>
</html>
