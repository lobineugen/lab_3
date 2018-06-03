<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %><%--
  Created by IntelliJ IDEA.
  User: 2017
  Date: 31.05.2018
  Time: 23:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Search object</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript">
        function getAttr() {
            $.ajax({
                url : 'find',
                data : { "o": document.getElementById("o").value, "ot":  document.getElementById("ot").value},
                success : function(data) {
                    $('#parameters').html(data);
                }
            });
        };

        function info_check() {
            if(checkBoxChecked() !== 1) {
                alert("Select only one object for see info!");
                return false;
            }
            return true;
        }

        function checkBoxChecked() {
            return $("input[name='object_id']:checked").length;
        }
    </script>

    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
    </style>
</head>
<body>
<form method="post">
        <%HashMap<Integer, String> map = (HashMap<Integer, String>) request.getAttribute("list");%>
    <label>
        Enter object name:
        <input type="text" name="object_name" id="o" required/>
        Choose object type:
        <select name="object_type" id="ot">
            <% for(Map.Entry<Integer, String> entry : map.entrySet()) {%>
            <option value="<%=entry.getKey()%>"><%=entry.getValue()%></option>
            <% } %>
        </select>
    </label>
            <input type="button" onclick="getAttr()" value="Find">
            <input type="submit" formaction="info" value="Info" onclick="return info_check()">
            <div id="parameters"></div>
</body>
</html>