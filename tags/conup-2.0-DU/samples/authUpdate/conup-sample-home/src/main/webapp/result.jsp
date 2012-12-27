<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.osoa.org/sca/sca_jsp.tld" prefix="sca" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<sca:reference name="service" type="cn.edu.nju.conup.sample.home.services.PortalService" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Result page</title>
</head>
<body>
<h1 style="margin-bottom:30px;">Result Page</h1>
<%
	String userName = request.getParameter("userName");
	String passwd = request.getParameter("passwd");
	List<String> userInfos = service.execute(userName, passwd);
	for(String str : userInfos){
		
%>
<%=str %>
<% out.println("<br>"); %>
<%
	}
%>
</body>
</html>