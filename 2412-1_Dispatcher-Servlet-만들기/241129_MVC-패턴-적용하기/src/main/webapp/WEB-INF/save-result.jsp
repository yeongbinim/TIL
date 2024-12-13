<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<h1>Member Saved Successfully</h1>
<ul>
    <li>id: ${ member.getId() }</li>
    <li>name: ${ member.getName() }</li>
    <li>password: ${ member.getPassword() }</li>
</ul>
<a href="../members">전체 보기</a>
</body>
</html>