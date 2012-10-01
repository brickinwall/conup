<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.osoa.org/sca/sca_jsp.tld" prefix="sca" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<sca:reference name="notifyService" type="cn.edu.nju.moon.conup.domain.services.NotifyService" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Update Configuration</title>
</head>
<body>
<h1 style="margin-bottom:30px;">Update Configuration</h1>
<%
	boolean updateResult = notifyService.notifyInterceptor();
		
%>
<%
	out.println("<br>"); 
	out.println("updateResult = " + updateResult); 
	
%>
</body>
</html>