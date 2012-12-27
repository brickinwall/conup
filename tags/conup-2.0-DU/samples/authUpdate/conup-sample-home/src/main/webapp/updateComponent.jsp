<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.osoa.org/sca/sca_jsp.tld" prefix="sca" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<sca:reference name="updateComponentService" type="cn.edu.nju.moon.conup.domain.services.ComponentUpdateService" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Update Configuration</title>
</head>
<body>
<h1 style="margin-bottom:30px;">Update Configuration</h1>

<%
	String baseDir = "/home/nju/workspace/conup-sample-auth/target/classes";
	String classpath = "cn.edu.nju.moon.conup.sample.auth.services.AuthServiceImpl";
	String contributionURI = "conup-sample-auth";
	String compositeURI = "auth.composite";
	boolean updateResult = false;
	updateComponentService.update(baseDir, classpath, contributionURI, compositeURI);
		
%>
<%
	out.println("<br>"); 
	out.println("updateResult = " + updateResult); 
	
%>
</body>
</html>