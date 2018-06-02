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
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript">
        function getAttr() {
            $.ajax({
                url : 'params',
                data : 'ot=' + document.getElementById("ot").value,
                success : function(data) {
                    $('#parameters').html(data);
                    $('.display').show();
                }
            });
        };
    </script>
    <style>
        .display{
            display: none;
        }
    </style>
</head>
<body>
<form method="post">
    <%ArrayList<String> array = (ArrayList<String>) request.getAttribute("array");%>
    <label>
        Name:
        <input type="text" name="objectName" required>
        <input type="hidden" name="parentId" value="<%=array.get(0)%>">
        <select name="objectType" id="ot" onselect="getAttr()" onclick="getAttr()">
            <% if (array.size() == 1) { %>
            <option value="1" selected>University</option>
            <% } %>
            <% for (int i = 1; i < array.size(); i++) {%>
            <option value="<%=array.get(i++)%>"><%=array.get(i)%></option>
            <%} %>
        </select>
    </label>
    <div id="parameters"> </div>
    <input type="submit" formaction="create" value="Create" class="display">
</form>

</body>
</html>