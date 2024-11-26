<%@ page import="servlet.MemberRepository" %>
<%@ page import="servlet.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    MemberRepository memberRepository = MemberRepository.getInstance();
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    Member member = new Member(null, username, password);
    memberRepository.save(member);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<h1>Member Saved Successfully</h1>
<ul>
    <li>id: <%=member.getId()%></li>
    <li>name: <%=member.getName()%></li>
    <li>password: <%=member.getPassword()%></li>
</ul>
<a href="/jsp/members.jsp">전체 보기</a>
</body>
</html>