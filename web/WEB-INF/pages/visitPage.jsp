<%@ page import="java.util.Map" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 02.06.2018
  Time: 14:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Students visit</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/datejs/1.0/date.min.js"></script>
    <script type="text/javascript">
        function showStudents() {
            $.ajax({
                url : 'lesson',
                data : 'lesson=' + document.getElementById("lessons").value,
                success : function(data) {
                    $('#table').html(data);
                    $('.display').show();
                }
            });
        };


        function addDate() {
            var date = Date.parse(document.getElementById("date").value).toString("dd.MM.yyyy");
            if (date !== '') {
                var tbl = document.getElementById('my-table'), // table reference
                    i;
                // open loop for each row and append cell
                for (i = 0; i < tbl.rows.length; i++) {
                    if (i===0) {
                        createCell(tbl.rows[i].insertCell(tbl.rows[i].cells.length), date, '');
                    } else {
                        var id  = document.getElementById('object'+i).getAttribute('value');
                        createCell(tbl.rows[i].insertCell(tbl.rows[i].cells.length), '', id+'_'+date);
                    }
                }
            }

        }

        function createCell(cell, text, name) {
            var checkbox = document.createElement('input');
            var div = document.createElement('div');
            var txt = document.createTextNode(text);
            if (text === '') {
                checkbox.setAttribute('type','text');
                checkbox.setAttribute('name',name);
                cell.appendChild(checkbox);
            } else {
                div.appendChild(txt);
                div.setAttribute('id','date');
                cell.appendChild(div);
            }

        }
    </script>
    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
        .display{
            display: none;
        }
    </style>
</head>
<body>
<form method="post">
    <label>
        <select name="lessons" id="lessons">
            <%Map<Integer, String> lessons = (Map<Integer, String>) request.getAttribute("lessons"); %>
            <%for (Map.Entry<Integer, String> temp : lessons.entrySet()) {%>
            <option value="<%=temp.getKey()%>"><%=temp.getValue()%>
            </option>
            <%} %>
        </select>
    </label>
    <input type="button" value="Select" onclick="showStudents()">
    <input type="date" name="date" id="date" class="display" >
    <input type="button" onclick="addDate()" value="Add date" class="display">
    <div id="table"></div>
    <input type="submit" value="Save" class="display" formaction="saveVisit">
    <input type="submit" value="Home" formaction="home">
</form>
</body>
</html>
