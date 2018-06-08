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
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script type="text/javascript">
        function studentLessons() {
            if ($("#lstBox2 > option").length) {
                var arrayId = "";
                $("#lstBox2 > option").each(function () {
                        arrayId+= $(this).val() + "/";
                    }
                );
                $.ajax({
                    url : 'lessonsName',
                    data : 'lessonsId=' + arrayId,
                    success : function(data) {
                        var newArr = data.split(";");
                        for( var i = 0; i <newArr.length-1; i++) {
                            var x = newArr[i].toString().split(":");
                            $("#lstBox2 > option").each(function () {
                                    if($(this).val() === x[0]){
                                        $(this).text(x[1]);
                                        $(this).show();
                                    }
                                }
                            );

                        }
                    }
                });
            }
        }
    </script>
</head>
<body onload="studentLessons()">
<form method="POST">
    <input type="submit" formaction="back" value="Back">
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
            if (id==9) {
                break;
            }
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
    <%if (object.getObjectTypeID() == 4 ) {%>
    <div class="subject-info-box-2">
        Student lessons:<br>
        <select multiple="multiple" id='lstBox2' class="form-control" name="9">
            <%
                for (String key : keySet) {
                    int id = Integer.parseInt(key.substring(0, key.indexOf("_")));
                    List<String> list = (List<String>) object.getParams().get(key);
                    if (id==9) {
                        for (String temp : list) {%>

            <option value="<%=temp%>" hidden><%=temp%></option>
            <%            }
            }
            }

            %>
        </select>
    </div>
    <%} %>

</form>

</body>
</html>
