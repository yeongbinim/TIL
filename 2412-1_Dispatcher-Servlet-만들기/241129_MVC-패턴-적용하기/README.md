# MVC 패턴 적용하기

지난 시간에 서블릿으로만 해보고, JSP로만도 해보면서 느꼈던 아쉬운 점은 비즈니스 로직과 HTML이 같이 섞여 있다는 점이었다.

둘의 변경 주기가 다르기 때문에, 유지보수를 위해 둘이 분리하는 게 좋다고 생각했고, 아래와 같은 요구사항을 세웠다.



- 기술 요구사항
  - [x] 비즈니스로직은 java파일에서만 작성할 것
  - [x] JSP 파일에서 import문을 사용하지 말 것
  - [x] JSP 파일에 직접 접근하지 못하도록 할 것

### 목차

- [MVC 패턴 적용하기](#mvc-패턴-적용하기-1)
- [RequestDispatcher로 view에 forward하기](#requestdispatcher로-view에-forward하기)
- [JSP 작성하기](#jsp-작성하기)
- [개선된 점 \& 아쉬운 점](#개선된-점--아쉬운-점)
- [마치며](#마치며)

<br/>

### MVC 패턴 적용하기

관심사를 크게 3개로 분리한다.

- 모델: 뷰에 출력할 데이터를 담아둔다. 뷰가 필요한 데이터를 모두 모델에 담아서 전달해주는 덕분에 뷰는 비즈니스 로 직이나 데이터 접근을 몰라도 되고, 화면을 렌더링 하는 일에 집중할 수 있다.
- 뷰: 모델에 담겨있는 데이터를 사용해서 화면을 그리는 일에 집중한다. 여기서는 HTML을 생성하는 부분을 말한다.
- 컨트롤러: HTTP 요청을 받아서 파라미터를 검증하고, 비즈니스 로직을 실행한다. 그리고 뷰에 전달할 결과 데이터를 조회해서 모델에 담는다.



이전에는 아래와 같았다면

<div align="center"><img width="300" src="https://github.com/user-attachments/assets/48f6ace8-ec35-4510-88f9-9152df0bf0bf"></div>

이제는 아래의 구조로 할 것이다.

<div align="center"><img width="300" src="https://github.com/user-attachments/assets/00f249bd-6abf-4568-8d8b-93ed748fb156"></div>

기존에 만들어두었던 Servlet이 비즈니스 로직을 담당하는 Controller로써 데이터를 Model에 담아서 Jsp로 던져주도록 할 것이다.



### RequestDispatcher로 view에 forward하기

우선 Servlet코드에서 service 메서드를 수정해볼 것이다.

```java
import jakarta.servlet.RequestDispatcher;

protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  String username = request.getParameter("username");
  String password = request.getParameter("password");
  Member member = new Member(null, username, password);

  memberRepository.save(member);
  request.setAttribute("member", member);
  String viewPath = "/WEB-INF/views/save-result.jsp";
  RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
  dispatcher.forward(request, response);
}
```

위와같이 `request.getRequestDispatcher(경로)`를 통해 어떤 자원(JSP or Servlet)인지 경로를 지정하여 RequestDispatcher 구현체를 얻는다.

그리고, 그 구현체의 `.forward`를 호출하면 요청을 전달하고 그 자원을 실행시킨다.

그러면 전달된 자원이 응답 처리를 이어서 수행할 수 있는 것이다.

**마치... express의 미들웨어 같다는 느낌이 들었다. (express에서도 저 request에 값을 넣어서 다음 미들웨어에게 전달하곤 했다)**

이제 이 자원을 넘겨받을 JSP를 작성해보자.



### JSP 작성하기

우선 JSP를 `webapp/WEB-INF/` 로 이동할 것이다. 이 경로안에 JSP가 있으면 외부에서 직접 JSP를 조회할 수 없을 것이다.

아래는 save.jsp의 코드이다.

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><meta charset="UTF-8"></head>
<body>
<h1>Member Saved Successfully</h1>
<ul>
    <li>id: <%= (request.getAttribute("member")).id %></li>
    <li>name: ${ member.getName() }</li>
    <li>password: ${ member.getPassword() }</li>
</ul>
</body>
</html>
```

id를 불러오는 방식과 name을 불러오는 방식이 다른 것을 확인할 수 있다.

우선, `request.getAttribute("member")` 로 member값을 불러올 수 있다는 것을 기억하기 위해서 이렇게 적었고, jsp가 `${}` 문법을 제공하기 때문에, 바로 request의 attribute에 조회할 수 있다.

그런데 보통 Member 도메인의 id를 private으로 막아두기 때문에 getter로 접근해야하는데, 그러기 위해서는 저 Object 객체로 반환되는 request.getAttribute()를 Member로 형변환 시켜줘야 한다.

그러기위해서는 결국 Member를 import할 수 밖에 없으므로 `${}` 문법을 주로 적용했다.



### 개선된 점 & 아쉬운 점

이로써 컨트롤러의 역할과 뷰를 렌더링 하는 역할을 명확하게 구분할 수 있었다.

하지만 아직 아쉬운 점은... 컨트롤러는 굳이 매번 dispatcher를 꺼낸다거나... forward, 경로 지정, 검증.. 중복되는 것이 많다는 것이다.

메서드로 따로 빼는 것도 좋은 방법이지만, 이것도 까먹지 않고 매번 호출해야한다.

경로 지정도 매 메서드마다 다르게 해야하는데 jsp를 thymeleaf로 변경한다면 전체 코드를 다 변경해야 한다.

이런 공통 처리를 해주는 것이 필요하다!

이제 프론트 컨트롤러 패턴을 도입할 것이다. 드디어 디스패처 서블릿의 등장이다!!!!



### 마치며

이전에는 전체적인 흐름을 상상하며 내 멋대로 요구사항 만들고, 직접 만들어보고 그랬다면 이제는 김영한님 강의에서의 흐름을 50% 이상 참고해가면서, 빠르게 방향을 잡고 있다.

물론 그대로 따라하는 건 좋지 않다.

각 과정에서 '나라면 이렇게 할 텐데' 싶은 것들은 내 방식대로 해보고, '아 이래서 저렇게 했구나' 느끼고...

**이제 어느정도 나만의 방식이 생기는 것 같다.**

그리고 다시 느끼지만 김영한님 강의는 치트키인거 같다.

전에 내가 일주일 넘게 헤매던 부분을 김영한님 강의 듣고 하루만에 해결한 것 처럼 돌아갈 수 있는 길을 잘 잡아준다.

적극 활용해야겠다.

