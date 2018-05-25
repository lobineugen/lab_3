<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 25.05.2018
  Time: 20:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Add new object</title>
</head>
<body>
<form method="post">
    <%ArrayList<String> array = (ArrayList<String>) request.getAttribute("array");%>
    <label>
        Name:
        <input type="text" name="objectName">
        <input type="hidden" name="parentId" value="<%=array.get(0)%>">
        <select name="objectType" >
            <% if(array.size()==1) { %>
                <option value="null" selected>University</option>
            <% } %>
            <% for (int i = 1; i<array.size(); i++) {%>
            <option value="<%=array.get(i++)%>"><%=array.get(i)%></option>
            <%} %>
        </select>
    </label>
    <input type="submit" formaction="create" value="Create">
</form>
</body>
</html>
