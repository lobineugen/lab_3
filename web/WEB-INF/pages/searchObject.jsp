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
                type: "post",
                url : 'find',
                cache: false,
                data : { "o": document.getElementById("o").value, "ot":  document.getElementById("ot").value},
                success: function (data) {
                    var table  = document.createElement("table");
                    table.setAttribute("class","tbl");
                    var tr = document.createElement("tr");
                    var thNumber = document.createElement("th");
                    thNumber.appendChild(document.createTextNode("№"));
                    tr.appendChild(thNumber);
                    var thObjectId = document.createElement("th");
                    thObjectId.appendChild(document.createTextNode("ObjectID"));
                    tr.appendChild(thObjectId);
                    var thParentId = document.createElement("th");
                    thParentId.appendChild(document.createTextNode("ParentID"));
                    tr.appendChild(thParentId);
                    var thName = document.createElement("th");
                    thName.appendChild(document.createTextNode("Name"));
                    tr.appendChild(thName);
                    var thObjectTypeId = document.createElement("th");
                    thObjectTypeId.appendChild(document.createTextNode("ObjectTypeID"));
                    tr.appendChild(thObjectTypeId);
                    table.appendChild(tr);
                    $.each(data, function(index, object) {
                        tr = document.createElement("tr");
                        var td_id = document.createElement("td");
                        var input = document.createElement("input");
                        input.setAttribute("id","object_id");
                        input.setAttribute("type","checkbox");
                        input.setAttribute("name","object_id");
                        input.setAttribute("value",object.parentID + "_" + object.objectID);
                        td_id.appendChild(input);
                        tr.appendChild(td_id);
                        var object_id = document.createElement("td");
                        object_id.appendChild(document.createTextNode(object.objectID));
                        tr.appendChild(object_id);
                        var name = document.createElement("td");
                        name.appendChild(document.createTextNode(object.name));
                        tr.appendChild(name);
                        var parent_id = document.createElement("td");
                        parent_id.appendChild(document.createTextNode(object.parentID));
                        tr.appendChild(parent_id);
                        var object_type_id = document.createElement("td");
                        object_type_id.appendChild(document.createTextNode(object.objectTypeID));
                        tr.appendChild(object_type_id);
                        table.appendChild(tr);
                    });
                    $("#parameters").empty();
                    $("#parameters").append(table);
                }
            });
        }

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
        <input type="text" name="object_name" id="o"/>
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