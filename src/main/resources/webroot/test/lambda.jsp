<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Objects" %>
<%@ page import="java.io.IOException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <title>JSP with Lambda</title>
</head>
<body>

<h1>JSP with Lambda</h1>

<dl>
<%
    // JspWriter needs to be final to satisfy lambda requirements.
    final JspWriter jspOut = out;

    System.getProperties().entrySet().stream()
        .filter(entry -> entry.getKey().toString().matches("^(java|jdk|sun|user|file|os|sun)\\..*"))
        .sorted(Comparator.comparing(e -> e.getKey().toString()))
        .forEach(entry -> {
            try {
                String key = entry.getKey().toString();
                String value = Objects.toString(entry.getValue());
                jspOut.print("<dt>" + key + "</dt>");
                jspOut.println("<dd>" + value + "</dd>");
            } catch(IOException e) {
                e.printStackTrace(System.err);
            }
        }); %>
</dl>
</body>
</html>


