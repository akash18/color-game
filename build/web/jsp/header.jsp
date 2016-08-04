<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setBundle basename="com.game.config.config"/>
<fmt:message key="number.of.rows" var="rows"/>
<fmt:message key="number.of.columns" var="columns"/>
<fmt:message key="block.seconds" var="blockingDuration"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Color Game</title>

        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link href="css/style.css" rel="stylesheet">

        <script src="js/jquery.min.js"></script>
        <script src="js/jquery.uilock.js"></script>
        <script src="js/jquery.uilock.min.js"></script>
        <script src="js/bootstrap.min.js"></script>
        <script src="js/websocket.js"></script>
