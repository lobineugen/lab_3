<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 19.03.2018
  Time: 20:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page isErrorPage="true" %>
<html>
<head>
    <title>Error</title>
</head>
<body>
    <h3>Sorry, we have some problems...</h3>
    <p>
        Root cause of problem is <%=exception.getCause()%>
    </p>
    <p>
        Details: <%
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        out.println(stringWriter);
        printWriter.close();
        stringWriter.close();
    %>
    </p>
</body>
</html>
