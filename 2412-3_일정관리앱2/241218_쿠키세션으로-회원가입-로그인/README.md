# 쿠키 세션으로 회원가입 로그인

쿠키와 세션을 이용해 회원가입 로그인을 구현해보자

### Controller에서 회원가입, 로그인 할 때 세션에 값 추가해주기

서블릿이 제공하는 `HttpSession` 을 생성하면 `Cookie: JSESSIONID=12345` 이런 쿠키를 알아서 생성해준다.

클라이언트가 서버에 처음 연결할 때, 이 클라이언트를 구별할 수 있도록 고유한 세션 ID를 생성하는 덕분에 우리가 별도의 UUID를 생성하여 sessionId를 관리할 필요가 없는 것이다.

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<Void> register(
    @Valid @RequestBody MemberCreate memberCreate,
    HttpServletRequest req
  ) {
    Member member = authService.createMember(memberCreate);
    HttpSession session = req.getSession(true);
    session.setAttribute("memberId", member.getId());
    return ResponseEntity
      .created(URI.create("/members/" + member.getId()))
      .build();
  }
}
```

위와같이 `req.getSession(true)` 를 통해서 현재 요청하는 클라이언트에 대해 세션이 없다면 생성하도록 옵션을 주고, setAttribute로 세션에 값들을 저장해 두었다.

<div align="center"><img width="800" src="https://github.com/user-attachments/assets/80ed45be-1a6a-49f6-aae7-fb61502927eb" /></div>

postman으로 회원가입 요청을 보낸 결과 응답 Header에 `Set-Cookie` 가 들어있는 것을 확인할 수 있다.

### 인증 필터 추가하기

이제 서블릿이 지원해주는 필터를 통해서 디스패처 서블릿이 호출되기 전에 쿠키에 JSSESSIONID에 해당하는 세션을 불러와서 없다면 401 에러를 주고, 있다면 정상적으로 우리 컨트롤러가 호출되도록 할 것이다.

```java
@Component
public class AuthFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    HttpSession session = httpRequest.getSession(false);
    if (session == null || session.getAttribute("memberId") == null) {
      httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
      httpResponse.getWriter().write("Unauthorized");
      return;
    }

    Long memberId = (Long) session.getAttribute("memberId");
    request.setAttribute("memberId", memberId);
    chain.doFilter(request, response);
  }
}
```

`httpRequest.getSession(false)`를 통해 없으면 null을 반환하도록 했고, filter의 경우 request에 값을 직접 넣어줄 수가 있기 때문에 memberId를 setAttribute로 넣어 줬다.

나중에 컨트롤러에서 `@RequestAttribute` 로 Id를 바로 받을 수 있게 된다.

### 필터 등록하기

작성한 필터를 설정 정보에 등록하자

```java
@Configuration
public class WebConfig {

  @Bean
  public FilterRegistrationBean<AuthFilter> authFilterRegistrationBean(AuthFilter authFilter) {
    FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(authFilter);
    registrationBean.addUrlPatterns("/api/*");
    registrationBean.setOrder(1);
    return registrationBean;
  }
}
```

서블릿 필터를 등록하기 위한 FilterRegistrationBean 객체를 생성하고, 여기에 `.setFilter(필터)` 로 내가 작성한 필터를 넣어준다.

`addUrlPatterns()`로 /api/* 을 통해 /auth/ 로 시작하는 것은 통과가 되도록 했고,

`setOrder()`로 필터가 실행될 순서를 지정했다. 지금은 필터가 하나라서 아무 값이나 넣어도 되지만 원래는 가장 낮은 값부터 체이닝된다.


<div align="center"><img width="800" src="https://github.com/user-attachments/assets/243fc1ca-a50e-4cd5-ac38-7bcecf77416d" /></div>

이제 회원가입이나 로그인 하지 않은 사용자는 401 응답을 받게 된다.