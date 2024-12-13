# Servlet 

## HttpServletRequest

HTTP 요청 메시지를 개발자가 직접 파싱해서 사용해도 되지만, 그걸 매번 하면 로직에 집중하기 힘들다.

서블릿은 개발자가 HTTP 요청 메시지를 편리하게 사용할 수 있도록 개발자 대신에 HTTP 요청 메시지를 파싱한다.

그리고 그 결과를 `HttpServletRequest` 객체에 담아서 제공한다.

HttpServletRequest로부터 어떤 정보들을 추출할 수 있는지 알아보자.

###  HttpServletRequest를 통해서 HTTP 메시지의 start-line, header 정보 조회 방법

```java
private void printStartLine(HttpServletRequest request) {
  System.out.println("--- REQUEST-LINE - start ---");
  System.out.println("request.getMethod() = " + request.getMethod());
  System.out.println("request.getProtocol() = " + request.getProtocol());
  System.out.println("request.getScheme() = " + request.getScheme());
  
  System.out.println("request.getRequestURL() = " + request.getRequestURL());
  System.out.println("request.getRequestURI() = " + request.getRequestURI());
   
  System.out.println("request.getQueryString() = " +request.getQueryString());

  System.out.println("request.isSecure() = " + request.isSecure());
  System.out.println();
}
```

```
--- REQUEST-LINE - start ---
request.getMethod() = GET
request.getProtocol() = HTTP/1.1
request.getScheme() = http
request.getRequestURL() = http://localhost:8080/request-header
request.getRequestURI() = /request-header
request.getQueryString() = username=hello
request.isSecure() = false
--- REQUEST-LINE - end ---
```

```java
//Header 모든 정보
private void printHeaders(HttpServletRequest request) {
  System.out.println("--- Headers - start ---");

  Enumeration<String> headerNames = request.getHeaderNames();
  while (headerNames.hasMoreElements()) {
    String headerName = headerNames.nextElement();
    System.out.println(headerName + ": " + request.getHeader(headerName));
  }
  System.out.println("--- Headers - end ---");
  System.out.println();
}
```

```
--- Headers - start ---
host: localhost:8080
connection: keep-alive
cache-control: max-age=0
sec-ch-ua: "Chromium";v="88", "Google Chrome";v="88", ";Not A Brand";v="99"
sec-ch-ua-mobile: ?0
upgrade-insecure-requests: 1
user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_0) AppleWebKit/537.36
(KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36
accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/
webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
sec-fetch-site: none
sec-fetch-mode: navigate
sec-fetch-user: ?1
sec-fetch-dest: document
accept-encoding: gzip, deflate, br
accept-language: ko,en-US;q=0.9,en;q=0.8,ko-KR;q=0.7
--- Headers - end ---
```

```java
private void printHeaderUtils(HttpServletRequest request) {
  System.out.println("--- Header 편의 조회 start ---");
  System.out.println("[Host 편의 조회]");
  System.out.println("request.getServerName() = " + request.getServerName()); 

  System.out.println("request.getServerPort() = " + request.getServerPort()); 

  System.out.println("[Accept-Language 편의 조회]");
  request.getLocales().asIterator()
    .forEachRemaining(locale -> System.out.println("locale = " + locale));
  System.out.println("request.getLocale() = " + request.getLocale());

  System.out.println("[cookie 편의 조회]");
  if (request.getCookies() != null) {
    for (Cookie cookie : request.getCookies()) {
      System.out.println(cookie.getName() + ": " + cookie.getValue());
    }
  }
  System.out.println("[Content 편의 조회]");
  System.out.println("request.getContentType() = " + request.getContentType());
  System.out.println("request.getContentLength() = " + request.getContentLength());
  System.out.println("request.getCharacterEncoding() = " + request.getCharacterEncoding());
  System.out.println("--- Header 편의 조회 end ---");
  System.out.println();
```
```
--- Header 편의 조회 start ---
[Host 편의 조회]
request.getServerName() = localhost
request.getServerPort() = 8080

[Accept-Language 편의 조회] locale = ko
locale = en_US
locale = en
locale = ko_KR
request.getLocale() = ko

[cookie 편의 조회]

[Content 편의 조회]
request.getContentType() = null
request.getContentLength() = -1
request.getCharacterEncoding() = UTF-8
--- Header 편의 조회 end ---
```

아래의 기타 정보는 HTTP 메시지의 정보는 아니다.

```java
private void printEtc(HttpServletRequest request) {
  System.out.println("--- 기타 조회 start ---");
  System.out.println("[Remote 정보]");
  System.out.println("request.getRemoteHost() = " + request.getRemoteHost());
  System.out.println("request.getRemoteAddr() = " + request.getRemoteAddr());
  System.out.println("request.getRemotePort() = " + request.getRemotePort());
  System.out.println();
  System.out.println("[Local 정보]");
  System.out.println("request.getLocalName() = " + request.getLocalName());
  System.out.println("request.getLocalAddr() = " + request.getLocalAddr());
  System.out.println("request.getLocalPort() = " + request.getLocalPort());
  System.out.println("--- 기타 조회 end ---");
  System.out.println();
}
```

```
--- 기타 조회 start ---
[Remote 정보]
request.getRemoteHost() = 0:0:0:0:0:0:0:1
request.getRemoteAddr() = 0:0:0:0:0:0:0:1
request.getRemotePort() = 54305

[Local 정보]
request.getLocalName() = localhost
request.getLocalAddr() = 0:0:0:0:0:0:0:1
request.getLocalPort() = 8080
--- 기타 조회 end ---
```

### HTTP 요청 데이터

HTTP 요청 메시지를 통해 서버로 데이터를 전달하는 방법은 다음 3가지이다.
- **GET - 쿼리 파라미터**
  - /url**?username=hello&age=20**
  - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
- **POST - HTML Form**
  - content-type: application/x-www-form-urlencoded
  - 메시지 바디에 쿼리 파리미터 형식으로 전달 username=hello&age=20 예) 회원 가입, 상품 주문, HTML Form 사용
- **HTTP message body**에 데이터를 직접 담아서 요청
  - HTTP API에서 주로 사용, JSON, XML, TEXT
  - 데이터 형식은 주로 JSON 사용 (POST, PUT, PATCH)

[**GET - 쿼리 파라미터**]

```java
private void printQueryParameter(HttpServletRequest request) {
  request.getParameterNames().asIterator()
    .forEachRemaining(paramName -> System.out.println(paramName + "=" + request.getParameter(paramName)));
  System.out.println("[전체 파라미터 조회] - end");
  System.out.println();
  System.out.println("[단일 파라미터 조회]");
  String username = request.getParameter("username");
  System.out.println("request.getParameter(username) = " + username);
  String age = request.getParameter("age");
  System.out.println("request.getParameter(age) = " + age);
  System.out.println();
  System.out.println("[이름이 같은 복수 파라미터 조회]");
  System.out.println("request.getParameterValues(username)");
  String[] usernames = request.getParameterValues("username");
  for (String name : usernames) {
    System.out.println("username=" + name);
  }
}
```

요청 `http://localhost:8080/request-param?username=hello&username=kim&age=20`

```
[전체 파라미터 조회] - start username=hello
age=20
[전체 파라미터 조회] - end
[단일 파라미터 조회] request.getParameter(username) = hello request.getParameter(age) = 20
[이름이 같은 복수 파라미터 조회] request.getParameterValues(username) username=hello
username=kim
```

[**POST - HTML Form**]

`content-type: application/x-www-form-urlencoded`
`message body: username=hello&age=20`

앞서 GET에서 살펴본 쿼리 파라미터 형식과 같다.
따라서 **쿼리 파라미터 조회 메서드를 그대로 사용**하면 된다.

클라이언트(웹 브라우저) 입장에서는 두 방식에 차이가 있지만, 서버 입장에서는 둘의 형식이 동일하므로, `request.getParameter()` 로 편리하게 구분없이 조회할 수 있다.

정리하면 `request.getParameter()` 는 GET URL 쿼리 파라미터 형식도 지원하고, POST HTML Form 형식도 둘 다 지원한다.

> **[참고]** <br/>
> content-type은 HTTP 메시지 바디의 데이터 형식을 지정한다.<br/>
> **GET URL 쿼리 파라미터 형식**으로 클라이언트에서 서버로 데이터를 전달할 때는 HTTP 메시지 바디를 사용하 지 않기 때문에 content-type이 없다.<br/>
> **POST HTML Form 형식**으로 데이터를 전달하면 HTTP 메시지 바디에 해당 데이터를 포함해서 보내기 때문에 바디에 포함된 데이터가 어떤 형식인지 content-type을 꼭 지정해야 한다.<br/>
> 이렇게 폼으로 데이터를 전송하는 형 식을 `application/x-www-form-urlencoded` 라 한다.

[**HTTP 요청 데이터**]

inputStream은 byte 코드를 반환한다. byte 코드를 우리가 읽을 수 있는 문자(String)로 보려면 문자표 (Charset)를 지정해주어야 한다. 여기서는 UTF_8 Charset을 지정해주었다.

```java
private void printApiBodyText(HttpServletRequest request) {
  ServletInputStream inputStream = request.getInputStream();
  String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  System.out.println("messageBody = " + messageBody);
}
```
```
messageBody = hello
```

JSON 결과를 파싱해서 사용할 수 있는 자바 객체로 변환하려면 Jackson, Gson 같은 JSON 변환 라이브러리 를 추가해서 사용해야 한다.

스프링 부트로 Spring MVC를 선택하면 기본으로 Jackson 라이브러리 ( `ObjectMapper` )를 함께 제공한다.

```java
import com.fasterxml.jackson.databind.ObjectMapper;

private ObjectMapper objectMapper = new ObjectMapper();

private void printApiBodyJson(HttpServletRequest request) {
  ServletInputStream inputStream = request.getInputStream();
  String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  System.out.println("messageBody = " + messageBody);
  HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
  System.out.println("data.username = " + helloData.getUsername());
  System.out.println("data.age = " + helloData.getAge());
}
```

```
messageBody={"username": "hello", "age": 20}
data.username=hello
data.age=20
```

> [**참고**]<br/>
> HTML form 데이터도 메시지 바디를 통해 전송되므로 직접 읽을 수 있다. 하지만 편리한 파리미터 조회 기능 ( `request.getParameter(...)` )을 이미 제공하기 때문에 파라미터 조회 기능을 사용하면 된다.

## HttpServletResponse

- HTTP 응답 메시지 생성
  - HTTP 응답코드 지정
  - 헤더 생성
  - 바디 생성
- 편의 기능 제공
  - Content-Type, 쿠키, Redirect

### HTTP 응답 메시지 생성

```java
protected void service(HttpServletRequest request, HttpServletResponse
 response) {
  response.setStatus(HttpServletResponse.SC_OK); //200
  //[response-headers]
  response.setHeader("Content-Type", "text/plain;charset=utf-8");
  response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
  response.setHeader("Pragma", "no-cache");
  response.setHeader("my-header","hello");
  cookie(response);
  redirect(response);
  //[message body]
  PrintWriter writer = response.getWriter();
  writer.println("ok");
}
```


### Content 편의 메서드

```java
private void content(HttpServletResponse response) {
  //Content-Type: text/plain;charset=utf-8
  //Content-Length: 2
  //response.setHeader("Content-Type", "text/plain;charset=utf-8");
  response.setContentType("text/plain");
  response.setCharacterEncoding("utf-8");
  //response.setContentLength(2); //(생략시 자동 생성)
}
```

### 쿠키 편의 메서드

```java
private void cookie(HttpServletResponse response) {
  //Set-Cookie: myCookie=good; Max-Age=600;
  //response.setHeader("Set-Cookie", "myCookie=good; Max-Age=600");
  Cookie cookie = new Cookie("myCookie", "good"); 
  cookie.setMaxAge(600); //600초
  response.addCookie(cookie);
}
```

### redirect 편의 메서드

```java
private void redirect(HttpServletResponse response) throws IOException {
  //Status Code 302
  //Location: /basic/hello-form.html
  //response.setStatus(HttpServletResponse.SC_FOUND); //302
  //response.setHeader("Location", "/basic/hello-form.html");
  response.sendRedirect("/basic/hello-form.html");
}
```

### HTTP 응답 바디

- 단순 텍스트 응답
  - 앞에서 살펴봄 ( `writer.println("ok");` )
- HTML 응답
- HTTP API - MessageBody JSON 응답

[**HTML 응답**]

HTTP 응답으로 HTML을 반환할 때는 content-type을 `text/html` 로 지정해야 한다.

```java
protected void service(HttpServletRequest request, HttpServletResponse
 response) {
  //Content-Type: text/html;charset=utf-8
  response.setContentType("text/html");
  response.setCharacterEncoding("utf-8");
  PrintWriter writer = response.getWriter(); writer.println("<html>"); writer.println("<body>");
  writer.println(" <div>안녕?</div>"); writer.println("</body>"); writer.println("</html>");
}
```

[**JSON 응답**]

HTTP 응답으로 JSON을 반환할 때는 content-type을 `application/json` 로 지정해야 한다.
Jackson 라이브러리가 제공하는 `objectMapper.writeValueAsString()` 를 사용하면 객체를 JSON 문자로 변경할 수 있다.

```java
protected void service(HttpServletRequest request, HttpServletResponse
 response) {
  //Content-Type: application/json
  response.setHeader("content-type", "application/json");
  response.setCharacterEncoding("utf-8");
  HelloData data = new HelloData();
  data.setUsername("kim");
  data.setAge(20);
  //{"username":"kim","age":20}
  String result = objectMapper.writeValueAsString(data);
  response.getWriter().write(result);
}
```

> [**참고**] <br/>
> `application/json` 은 스펙상 utf-8 형식을 사용하도록 정의되어 있다. 그래서 스펙에서 charset=utf-8 과 같은 추가 파라미터를 지원하지 않는다. <br/>
> 따라서 `application/json` 이라고만 사용해야지 `application/json;charset=utf-8` 이라고 전달하는 것은 의미 없는 파라미터를 추가한 것이 된다. <br/>
> response.getWriter()를 사용하면 추가 파라미터를 자동으로 추가해버린다. 이때는 response.getOutputStream()으로 출력하면 그런 문제가 없다.