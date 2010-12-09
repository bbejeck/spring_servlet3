<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %> 

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Search Results</title>
    </head>
    <body>
        <h1>Search Results Returned </h1>
        <div id="results">
            <c:out value="${result}"/>
        </div>
    </body>
</html>
