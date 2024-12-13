# Servlet과 JSP사용하기

이전에는 Apache만으로 CGI구현체를 작성하여  회원을 추가하고 목록을 조회할 수 있는 동적 웹을 만들었다.

그 과정에서 요청에 대해 스레드로 처리하고 풀을 관리한다거나, 인스턴스를 싱글턴으로써 생명주기를 관리해주는 것이 필요하다는 것을 느꼈고, 바로 톰캣이 그것을 해결해 준다는 것을 알았다.

스프링 부트에는 기본적으로 톰캣이 내장되어 있다고 한다. 이렇게 스프링 부트에 너무 많은 설정들이 편하게 세팅되어 있다보니, 정확히 어디까지가 스프링이 지원해주는 기능이고, 어디까지가 톰캣이 지원해 주는 건지 감이 잡히지 않았다.

스프링 부트의 도움 없이 톰캣에 서블릿 구현체를 올려서 실행시키는 것부터 차근차근 경험해보고 싶었다.



새로운 요구사항은 다음과 같다.

- 기술 요구사항
  - [x] Servlet 구현체를 작성하여, 빌드된 war를 직접 톰캣에 올려서 실행시킨다.
  - [x] Servlet 구현체 없이 JSP만으로 대체한다.



<br/>

## 목차

- [수동으로 war 옮겨서 실행하기](#수동으로-war-옮겨서-실행하기)
  - [Servlet 구현체 작성](#servlet-구현체-작성)
  - [web.xml로 서블릿 매핑하기](#webxml로-서블릿-매핑하기)
  - [빌드된 war 수동으로 tomcat에 넣기](#빌드된-war-수동으로-tomcat에-넣기)
- [배포 과정을 더 편리하게](#배포-과정을-더-편리하게)
  - [@WebServlet으로 매핑하기](#webservlet으로-매핑하기)
  - [Intellij에 톰캣 연동하기](#intellij에-톰캣-연동하기)
- [JSP로 바꿔보기](#jsp로-바꿔보기)
  - [JSP 코드 작성](#jsp-코드-작성)
  - [아쉬운 점](#아쉬운-점)
- [마치며](#마치며)




<br/>

## 수동으로 war 옮겨서 실행하기

### Servlet 구현체 작성

우선 api들을 불러오기 위해 gradle의 dependencies에 아래를 추가한다.

```yaml
implementation 'jakarta.servlet:jakarta.servlet-api:5.0.0'
```

주의 할 점은 최근 톰캣 버전에서는 `javax.*` 패키지 대신 `jakarta.*` 패키지를 사용하기 때문에, 톰캣 10.x 이상을 사용한다면 `jakarta.servlet-api`를 사용해야 한다. (처음에 이거 때문에 애먹었다.)

그리고 지난 번에 작성한 CGI 구현체를 아래와 같이 수정하였다.

<div align="center"><img width="657" alt="스크린샷 2024-11-27 오전 12 45 27" src="https://github.com/user-attachments/assets/4cb760de-27f9-4295-860c-9419834fb7fe"/></div>

[[전체 코드 보러 가기]](./src/main/java/servlet/)

CGI 구현체를 작성하여, Apache 웹 서버가 실행시키도록 했을 때와 다르게, Servlet 구현체를 작성하면서 달라진 점은 위와 같다.

1. HttpServlet을 구현해야 한다.
2. HttpServlet을 구현할때 service메서드를 (이외에도 doGet, doPost...등이 있다) Override하면 req 객체와 resp객체를 파라미터로 받는다.
3. req 객체로부터 getParameter로 본문 파라미터를 가져올 수 있다.
4. 표준 출력을 하는게 아니라 resp 객체 내에 있던 PrintWriter 객체를 가져와서 여기에 HTTP 응답 본문을 작성한다.

<br/>

### web.xml로 서블릿 매핑하기

`/webapp/WEB-INF` 내에 web.xml을 만들어 직접 서블릿을 매핑했다.

시작 부분의 XML 네임스페이스와 스키마 위치 정의는 이 문서가 Java EE 웹 애플리케이션을 위한 설정 파일임을 명시하고, 이 파일이 Java EE의 `web-app` 스키마에 따라 유효하게 작성되어야 함을 보장하기 위해 필요한 것이다.

(외우지 말고 그냥 복붙하자)

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
  <servlet>
    <servlet-name>memberFormServlet</servlet-name>
    <servlet-class>m1126.MemberFormServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>memberFormServlet</servlet-name>
    <url-pattern>/members/new-form</url-pattern>
  </servlet-mapping>
  
  <servlet>
    <servlet-name>memberSaveServlet</servlet-name>
    <servlet-class>m1126.MemberSaveServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>memberSaveServlet</servlet-name>
    <url-pattern>/members/save</url-pattern>
  </servlet-mapping>
  
  <servlet>
    <servlet-name>memberListServlet</servlet-name>
    <servlet-class>m1126.MemberListServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>memberListServlet</servlet-name>
    <url-pattern>/members</url-pattern>
  </servlet-mapping>
</web-app>
```

그리고, 각 Servlet마다 `<servlet>` 로 서블릿 선언을 해주었고, `<servlet-mapping>` 으로 서블릿이 응답할 URL 패턴을 매핑해주었다.

<br/>

### 빌드된 war 수동으로 tomcat에 넣기

정말 간단하다. [아파치 톰캣 공식 사이트](https://tomcat.apache.org/)에서 톰캣을 내려 받아서, 빌드된 war를 webapps 폴더에 넣고 해당 톰캣을 실행시켰다.

<img width="1065" alt="스크린샷 2024-11-27 오전 1 15 19" src="https://github.com/user-attachments/assets/e4c57dc9-c0b5-45dc-92c1-a0ad46299fad">

손쉽게 서버가 실행이 되었다.

압축파일을 어떻게 저렇게 실행시킬 수 있는거지? 하고 궁금해서 서버가 켜져있는 상태에서 war파일을 webapps에 넣어봤다.

<div align="center"><img src="https://github.com/user-attachments/assets/fc697b3d-0fc9-4a86-b94d-202ffd4c16bc" width="300"/></div>

톰캣 서버가 실행중일때에는 webapps 디렉터리의 변화를 감지하여, 새로운 WAR 파일이 추가되면 이를 자동으로 압축 해제해서 배포하는 방식이었다.

더 알아보니 `erver.xml` 파일에 설정된 `Host` 태그의 `watchedResource` 속성과 `backgroundProcessorDelay` 속성에 의해 결정된다고 한다.

쨌든 이렇게 수동으로 배포 성공!

<br/>

## 배포 과정을 더 편리하게

### @WebServlet으로 매핑하기

`/webapp/WEB-INF/web.xml` 을 일일이 작성하는건 정말이지 말도 안된다고 생각했고, 알아보니 `@WebServlet` 애너테이션을 서블릿 구현체에 달아서 편하게 매핑이 가능하더라.

서블릿 3.0 이상에서 지원되는 기능 (서블릿 3.0 사양을 지원하는 Apache Tomcat의 버전은 Tomcat 7.0부터)이라고 한다.

```java
@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
public class MemberFormServlet extends HttpServlet {
  ...
```

이런식으로 편리하게 매핑해줄 수 있다!!

훨씬 더 편해지는 것을 느꼈다.

<br/>

### Intellij에 톰캣 연동하기

서버에 배포할 때에는 인정. 하지만, 개발 단계에서 매번 이렇게 넣으라고....? 그건 정말 말도 안된다.

하지만 나는 IntelliJ Ultimate 버전을 사용하고 있다.

분명히 재생 버튼을 누르면 빌드된 이후의 war파일을 톰캣 webapps 폴더에 넣어준 뒤 bin폴더의 실행 스크립트를 대신 실행시켜줄 것이다.

<img width="897" alt="스크린샷 2024-11-26 오후 7 47 07" src="https://github.com/user-attachments/assets/1f3fd924-301b-4904-b17b-8ebf56ce7639">

1. 상단 메뉴에서 **Run** > **Edit Configurations...** 를 선택

2. 왼쪽 상단의 **+** 버튼을 클릭하고, **Tomcat Server** > **Local** 을 선택

3. **Server** 탭에서 **Configure...** 버튼을 클릭하여 설치한 Tomcat 위치 명시

<br/>

위의 과정을 거치고 실행을 누르니...! 와 실행할때마다 크롬의 탭이 열리며... 결과물이 나오는데...

이 얼마나 편한가... 행복..

<br/>

## JSP로 바꿔보기

지금의 Servlet 코드에는 너무나도 큰 아쉬움이 있다.

저 HTML 태그들을 순전히 문자열로 작업해야한다는 점... 서블릿 코드 내에서 HTML을 직접 작성해야 하는 번거로움이 어마무시하게 불편하다.

여기서 JSP가 등장하는데, HTML 코드 내에 Java 코드를 삽입할 수 있는 방식이다. (여기서 JSP는 내부적으로 서블릿으로 변환되어 실행된다.)

이 JSP를 활용한 코드로 바꿔보자!

<br/>

### JSP 코드 작성

우선 JSP api 의존성을 아래와 같이 추가하자.

```java
implementation 'jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.0.0'
```

스프링부트같이 내장 톰켓서버를 사용하는 경우, JSP 파일을 처리할 Jasper JSP 엔진이 포함되어 있지 않아서 아래의 의존성도 추가해야 한다고 한다.

```java
implementation 'org.apache.tomcat.embed:tomcat-embed-jasper:10.1.0'
```

여기서 아까의 MemberSaveServlet을 JSP로 작성하면 아래와 같다.

```jsp
<%@ page import="m1126.MemberRepository" %>
<%@ page import="m1126.Member" %>
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
</body>
</html>
```

[[전체 코드 보러 가기]](./src/main/webapp/jsp/)


- `<%@ page %>` 는 지시자로 JSP 페이지에 대한 설정 정보를 제공한다.
- `<% %>`는 Scriptlets으로 실제 Java코드를 작성할 때 사용된다.
- `<%= %>` 는 Expressions으로 출력을 생성하는데 사용되며, out.print()` 메소드를 호출하는 것과 같다.
- 이외에도 Declarations이라 불리는 변수나 메서드 선언에 사용되는 `<%! %>`가 있다.

최종적으로 훨씬 더 깔끔하게 html과 java코드를 같이 적을 수 있었다.

<br/>

### 아쉬운 점

서블릿에서도 느꼈지만, JSP에서도 아직 해결되지 못한 점은 비즈니스 로직과 HTML이 같이 섞여있다는 것이다.

서블릿만 작성했을 때에는 Java코드에 HTML 을 추가한 느낌이 들었으면,

JSP로만 작성했을 때에는 HTML에 Java코드만 추가한 느낌이랄까..?

<br/>

이게 왜 문제가 되냐! 하면

이 둘의 변경 주기가 다르기 때문이다.

변경 주기가 다르면 분리해야한다.

그렇게 MVC 패턴이 나왔다.

다음시간에는 이 MVC 패턴을 적용해볼 것이다.

<br/>



## 마치며

막 컴공으로 전과해서 들은 인터넷 프로그래밍 수업에서 이 톰캣을 연결했던 경험이 있었다는게 떠올랐다.

세상에나.. 그때는 그냥 강의자료 보고 차례대로 따라해서

뭔지도 모르고

'와 이클립스 미쳤다!' 하면서 나는 이 모든게 이클립스가 해주고 있는 줄 알았었다.

ㅋㅋㅋㅋㅋㅋ

이제는 메모장에 코드 작성해서 옮기는 과정도 하라면 할 수 있을 것 같다.

조금 귀찮기는 하겠지만..

여튼 이 불편함을 겪어야, 삽질을 해야 편안함을 누릴 자격을 얻는구나.. 생각이 든다.
