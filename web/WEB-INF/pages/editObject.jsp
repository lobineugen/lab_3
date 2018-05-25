<%@ page import="org.lab.three.beans.lwObject" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 25.05.2018
  Time: 23:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Edit object</title>
</head>
<body>
<form method="post">
    <%lwObject object = (lwObject) request.getAttribute("object"); %>
    <label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObject_id()%>" readonly>
    </label>
    <label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>">
    </label>
    <label>
        Parent id:
        <input type="text" name="parentId" value="<%=object.getParent_id()%>" readonly>
    </label>
    <label>
        Object type id:
        <input type="text" name="objectType" value="<%=object.getObject_id()%>" readonly>
    </label>
    <input type="submit" formaction="submitEdit" value="Save">
</form>
</body>
</html>
