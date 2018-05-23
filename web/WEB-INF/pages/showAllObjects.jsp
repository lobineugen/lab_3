<%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 21.05.2018
  Time: 23:15
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form>
    <table border="2">
        <tr>
            <th></th>
            <th>object_id</th>
            <th>parent_id</th>
            <th>object_type_id</th>
            <th>name</th>
        </tr>
        <c:forEach var="objects" items="${list}">
            <tr>
                <td><input type="checkbox" name="object_id" value="${objects.object_id}"></td>
                <td>${objects.object_id}</td>
                <td>${objects.parent_id}</td>
                <td>${objects.object_type_id}</td>
                <td><a href="children?object_id=${objects.object_id}">${objects.name}</a></td>
            </tr>

        </c:forEach>
        <input type="submit" formaction="add" value="Add new">
        <input type="submit" formaction="edit" value="Edit">
        <input type="submit" formaction="remove" value="Remove">
    </table>
</form>



</body>
</html>
