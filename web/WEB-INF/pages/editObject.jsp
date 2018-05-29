<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.Map" %>
<%--
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
<form method="get">
    <%LWObject object = (LWObject) request.getAttribute("object"); %>
    <p><label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObject_id()%>" readonly>
    </label></p>
    <p><label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>">
    </label></p>
    <%--<p><label>--%>
        <%--Parent id:--%>
        <%--<input type="text" name="parentId" value="<%=object.getParent_id()%>" readonly>--%>
    <%--</label></p>--%>
    <%--<p><label>--%>
        <%--Object type id:--%>
        <%--<input type="text" name="objectType" value="<%=object.getObject_id()%>" readonly>--%>
    <%--</label></p>--%>
    <p>Parameters:</p>
    <%for (Map.Entry<String, String> params : object.getParams().entrySet()) {%>
    <p><label>
        <%int id = Integer.parseInt(params.getKey().substring(0,params.getKey().indexOf("_"))); %>
        <%String name = params.getKey().substring(params.getKey().indexOf("_") + 1, params.getKey().length()); %>
        <%=name%>
        <input type="text" name="<%=id%>" value="<%=params.getValue()%>" required>
    </label></p>
    <%}%>
    <input type="submit" formaction="submitEdit" value="Save" OnClick="setAttr">
</form>
</body>
</html>
