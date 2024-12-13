<%@ page import="domain.Member" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<h1>Member List</h1>
<a href="members/new-form">회원 생성</a>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>password</th>
    </thead>
    <tbody>
    <% for (Member member : (List<Member>) request.getAttribute("members")) { %>
    <tr>
        <td><%= member.getId() %></td>
        <td><%= member.getName() %></td>
        <td><%= member.getPassword() %></td>
    </tr>
    <% } %>
    </tbody>
</table>

</body>
</html>