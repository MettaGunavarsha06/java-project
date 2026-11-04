<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String attemptId = request.getParameter("attemptId");
    request.getRequestDispatcher("/result-view.html" + (attemptId != null ? "?attemptId=" + attemptId : "")).forward(request, response);
%>

