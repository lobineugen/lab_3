<%@ page import="java.util.Map" %><%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 02.06.2018
  Time: 14:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Students visit</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/datejs/1.0/date.min.js"></script>
    <script type="text/javascript">

        function showStudents() {
            $("#table").empty();
            var students;
            var visits;
            var date;
            var lesson = document.getElementById("lessons").value;
            var table = document.createElement("table");
            table.setAttribute("class", "tbl");
            table.setAttribute("id", "my-table");
            $.ajax({
                type: "post",
                url: 'lesson',
                cache: false,
                data: 'lesson=' + lesson,
                success: function (data) {
                    students = data;
                    $.ajax({
                        type: "post",
                        url: 'lesson_visits',
                        cache: false,
                        data: 'lesson=' + lesson,
                        success: function (data) {
                            visits = data;
                            $.ajax({
                                type: "post",
                                url: 'lesson_date',
                                cache: false,
                                data: 'lesson=' + lesson,
                                success: function (data) {
                                    date = data;
                                    var tr = document.createElement("tr");
                                    var thName = document.createElement("th");
                                    thName.appendChild(document.createTextNode("Name"));
                                    tr.appendChild(thName);
                                    $.each(date, function (index, object) {
                                        var td = document.createElement("td");
                                        var div = document.createElement("div");
                                        div.setAttribute("class", "date");
                                        div.appendChild(document.createTextNode(object));
                                        td.appendChild(div);
                                        tr.appendChild(td);
                                    });
                                    table.appendChild(tr);
                                    var numb = 1;
                                    var count = 0;
                                    $.each(students, function (key, value) {
                                        var tr1 = document.createElement("tr");
                                        var td = document.createElement("td");
                                        var input = document.createElement("input");
                                        input.setAttribute("id", "object" + numb++);
                                        input.setAttribute("type", "hidden");
                                        input.setAttribute("name", "objectId");
                                        input.setAttribute("value", key);
                                        var span = document.createElement("span");
                                        span.appendChild(document.createTextNode(value));
                                        td.appendChild(input);
                                        td.appendChild(span);
                                        tr1.appendChild(td);
                                        $.each(date, function (index, d_object) {
                                            $.each(visits, function (v_index, v_object) {
                                                if (v_object.date === d_object && parseInt(v_object.objectId) === parseInt(key)) {
                                                    console.log("yes");
                                                    var td = document.createElement("td");
                                                    var input = document.createElement("input");
                                                    input.setAttribute("type", "text");
                                                    input.setAttribute("name", key + "_" + d_object);
                                                    input.setAttribute("value", v_object.mark);
                                                    td.appendChild(input);
                                                    tr1.appendChild(td);
                                                    count = 0;
                                                    return false;
                                                } else {
                                                    count = 1;
                                                }
                                            });
                                            if (count === 1) {
                                                var td = document.createElement("td");
                                                var input = document.createElement("input");
                                                input.setAttribute("type", "text");
                                                input.setAttribute("name", key + "_" + d_object);
                                                input.setAttribute("value", "-");
                                                input.setAttribute("readonly", "true");
                                                td.appendChild(input);
                                                tr1.appendChild(td);
                                                count = 0;
                                            }
                                        });
                                        table.appendChild(tr1);
                                    });
                                }
                            });
                        }
                    });
                }
            });
            $('#table').append(table);
            $('.display').show();
        }


        function addDate() {
            var date = Date.parse(document.getElementById("date").value).toString("dd.MM.yyyy");
            var array = document.getElementsByClassName("date");
            if (collectionContains(array, date)) {
                alert("You can not add this date, it is already in use!");
                return;
            }
            if (date !== '') {
                var tbl = document.getElementById('my-table'), // table reference
                    i;
                // open loop for each row and append cell
                for (i = 0; i < tbl.rows.length; i++) {
                    if (i === 0) {
                        createCell(tbl.rows[i].insertCell(tbl.rows[i].cells.length), date, '');
                    } else {
                        var id = document.getElementById('object' + i).getAttribute('value');
                        createCell(tbl.rows[i].insertCell(tbl.rows[i].cells.length), '', id + '_' + date);
                    }
                }
            }

        }

        function collectionContains(collection, searchText) {
            for (var i = 0; i < collection.length; i++) {
                if (collection[i].innerText.toLowerCase().indexOf(searchText) > -1) {
                    return true;
                }
            }
            return false;
        }

        function createCell(cell, text, name) {
            var checkbox = document.createElement('input');
            var div = document.createElement('div');
            var txt = document.createTextNode(text);
            if (text === '') {
                checkbox.setAttribute('type', 'text');
                checkbox.setAttribute('name', name);
                cell.appendChild(checkbox);
            } else {
                div.appendChild(txt);
                div.setAttribute('class', 'date');
                cell.appendChild(div);
            }

        }
    </script>
    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
        .display {
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
    <%if (session.getAttribute("right").equals("FULL") || session.getAttribute("right").equals("EDIT")) {%>
    <input type="date" name="date" id="date" class="display">
    <input type="button" onclick="addDate()" value="Add date" class="display">
    <% } %>
    <div id="table"></div>
    <%if (session.getAttribute("right").equals("FULL") || session.getAttribute("right").equals("EDIT")) {%>
    <input type="submit" value="Save" class="display" formaction="saveVisit">
    <%} %>
    <input type="submit" value="Home" formaction="home">
</form>
</body>
</html>
