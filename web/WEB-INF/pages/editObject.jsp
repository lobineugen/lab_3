<%@ page import="org.lab.three.beans.LWObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: Lobin Eugene
  Date: 25.05.2018
  Time: 23:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="errorPage.jsp" %>
<html>
<head>
    <title>Edit object</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#btnRight').click(function(e) {
                var selectedOpts = $('#lstBox1 option:selected');
                if (selectedOpts.length == 0) {
                    alert("Nothing to move.");
                    e.preventDefault();
                }

                $('#lstBox2').append($(selectedOpts).clone());
                $(selectedOpts).remove();
                e.preventDefault();
            });

            $('#btnLeft').click(function(e) {
                var selectedOpts = $('#lstBox2 option:selected');
                if (selectedOpts.length == 0) {
                    alert("Nothing to move.");
                    e.preventDefault();
                }

                $('#lstBox1').append($(selectedOpts).clone());
                $(selectedOpts).remove();
                e.preventDefault();
            });

            $("#SaveButton").on("click", function(){
                $('#lstBox2 option').prop("selected", true);
                if ($('#lstBox2 > option').length === 0) {
                    $('#lstBox2').append($('<option>', {
                        value: 0,
                        hidden: true,
                        selected: true
                    }));
                }
                $("#editForm").submit();
            });
        });
    </script>
    <script type="text/javascript">

        var studentLessonsArray = "";

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
                                        studentLessonsArray += x[1] + ",";
                                    }
                                }
                            );
                        }
                    }
                });
            }
            allLessons();

        }

        function allLessons() {
            if ($("#lstBox1").length) {
                $.ajax({
                    url : 'allLessons',
                    success : function(data) {
                        var newArr = data.split(";");
                        for( var i = 0; i <newArr.length-1; i++) {
                            var x = newArr[i].toString().split(":");
                            if (!studentLessonsArray.includes(x[1]+",")) {
                                $('#lstBox1').append($('<option>', {
                                    value: x[0],
                                    text: x[1]
                                }));
                            }


                        }
                    }
                });
            }
        }

    </script>

    <style>
        <%@include file="/WEB-INF/css/styles.css"%>
    </style>
</head>
<body onload="studentLessons()">
<form method="POST" id="editForm" >
    <%LWObject object = (LWObject) request.getAttribute("object"); %>
    <input type="submit" formaction="submitEdit" value="Save" id="SaveButton">
    <input type="submit" formaction="back" value="Back">
    <p><label>
        Object id:
        <input type="text" name="objectId" value="<%=object.getObjectID()%>" class="lft" readonly>
    </label></p>
    <p><label>
        Name:
        <input type="text" name="name" value="<%=object.getName()%>" class="lft">
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
                        <input type="text" name="<%=id%>" value="<%=temp%>" class="lft" <%=id==9 ? "hidden" : ""%>>
                    </label>
                </p>
    <%} %>
    <%}%>
    <%if (object.getObjectTypeID() == 4 ) {%>
        <div class="subject-info-box-1">
            All lessons:<br>
            <select multiple="multiple" id='lstBox1' class="form-control">
            </select>
        </div>

        <div class="subject-info-arrows text-center">
            <br />
            <input type='button' id='btnRight' value ='  >  '/>
            <br/><input type='button' id='btnLeft' value ='  <  '/>
        </div>
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
