<%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 21.05.2018
  Time: 22:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Start page</title>
</head>
<body>
<%session.invalidate();%>
<%request.getSession(true);%>

<form method="POST" action="sign">
    <label>
        Enter your name:
        <input type="text" name="userName" required/>
    </label>
    <input type="submit">
</form>
</body>
</html>
