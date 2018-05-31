<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.Map" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 31.05.2018
  Time: 22:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Object info</title>
</head>
<body>
<form method="POST">
    <%LWObject object = (LWObject) request.getAttribute("object"); %>
    <p><label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObject_id()%>" readonly>
    </label></p>
    <p><label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>" readonly>
    </label></p>
    <p>Parameters:</p>
    <%for (Map.Entry<String, String> params : object.getParams().entrySet()) {%>
    <p><label>
        <%int id = Integer.parseInt(params.getKey().substring(0, params.getKey().indexOf("_"))); %>
        <%String name = params.getKey().substring(params.getKey().indexOf("_") + 1, params.getKey().length()); %>
        <%=name%>
        <input type="text" name="<%=id%>" value="<%=params.getValue()%>" readonly>
    </label></p>
    <%}%>
    <input type="submit" formaction="back" value="Back">

</form>

</body>
</html>
