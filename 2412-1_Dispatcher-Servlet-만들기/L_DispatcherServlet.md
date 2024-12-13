# Dispatcher Servlet

## Dispatcher Servlet과 전체 흐름

스프링은 하나의 서블릿을 등록하는데, 모든 경로에 대해서 매핑한다. 이 디스패처 서블릿이 SpringMVC의 핵심이다.

(그런데 더 자세한 경로가 우선순위가 높기때문에 새로 서블릿을 등록하면 동작이 가능하다.)

<br/>

### 클라이언트 요청시 Dispatcher Servlet이 어떻게 호출되는지

- 클라이언트에서 HTTP 요청시 HttpServlet 이 제공하는 `service()` 가 호출된다.

- 스프링 MVC는 DispatcherServlet의 부모인 FrameworkServlet 에서 service() 를 오버라이드 해두었다.
- FrameworkServlet.service() 를 시작으로 여러 메서드가 호출되면서 DispatcherServlet.doDispatch() 가 호출된다.

<br/>

### `DispatcherServlet.doDispatch()` 호출시 동작 순서

<div align="center"><img width="400" alt="스크린샷 2024-12-02 오후 1 18 13" src="https://github.com/user-attachments/assets/e770cdb0-6091-4b19-a652-54fadfe055fb"></div>

- 핸들러 실행 단계
  1. 핸들러 조회: **핸들러 매핑**을 통해 URL에 매핑된 **핸들러**(컨트롤러)를 조회한다.
  2. 핸들러 어댑터 조회: 핸들러를 실행할 수 있는 **핸들러 어댑터**를 조회한다.
  3. 핸들러 어댑터 실행: `handle(handler)`로 핸들러 어댑터를 실행한다.
  4. 핸들러 실행: 3번에서 인자로 넘어간 handler가 핸들러 어댑터에 의해 호출된다.
- 응답 반환 단계
  - ModelAndView 반환: 핸들러 어댑터는 핸들러가 반환하는 정보를 **ModelAndView**로 변환해서 반환한다.
  - viewResolver 호출: **뷰 리졸버**를 찾아 실행한다. 
  - View 반환: viewResolver가 뷰의 논리 이름을 물리 이름으로 바꾸고, 렌더링 역할의 **View 객체를 반환**한다.
  - 뷰 렌더링: view.render() 를 통해 뷰를 렌더링 한다.

<div align="center"><img width="350" alt="스크린샷 2024-12-02 오후 1 34 54" src="https://github.com/user-attachments/assets/20a36c63-6902-4467-9c1a-065365bc6054"></div>

<br/>

### 확장성 좋은 Dispatcher Servlet

스프링 MVC의 큰 강점은 `DispatcherServlet` 코드의 변경 없이, 원하는 기능을 변경하거나 확장할 수 있다는 점이다. 확장 가능할 수 있게 인터페이스로 제공한다.

이 인터페이스들만 구현해서 `DispatcherServlet` 에 등록하면 나만의 컨트롤러를 만들수도 있다.

**주요 인터페이스 목록**

- 핸들러 매핑: `org.springframework.web.servlet.HandlerMapping`
- 핸들러 어댑터: `org.springframework.web.servlet.HandlerAdapter`
- 뷰 리졸버: `org.springframework.web.servlet.ViewResolver`
- 뷰: `org.springframework.web.servlet.View`

사실 해당 기능을 직접 확장하거나 나만의 컨트롤러를 만드는 일은 없으므로 걱정하지 않아도 된다.

왜냐하면 스프링 MVC는 전세계 수 많은 개발자들의 요구사항에 맞추어 기능을 계속 확장해왔고, 그래서 웹 애플리케이션을 만들 때 필요로 하는 대부분의 기능이 이미 다 구현되어 있다.

그래도 이렇게 핵심 동작방식을 알아두어야 향후 문제가 발생했을 때 어떤 부분에서 문제가 발생했는지 쉽게 파악하고, 문제를 해결할 수 있다. 그리고 확장 포인트가 필요할 때, 어떤 부분을 확장해야 할지 감을 잡을 수 있다.



<br/>

## Handler Mapping, Handler Adaptor

Controller를 작성할 때마다 개발자가 매번 Handler Mapping에 등록해주는 건 너무 불편하다.

따라서, 스프링은 실행시 리플렉션을 통해서 Handler들을 Handler Mapping에 등록을 시키는데, 이때 여러 방식으로 Handler를 설정할 경우에 대비해 우선순위를 적용하여 RequestMappingHandler와 BeanNameUrlHandler를 분리하여 등록시켜 두었다.

Handler Adaptor의 경우에도 어댑터들의 supports 를 물어볼때 우선순위가 있는데, 그것들을 알아보도록 하자.

<br/>


### 핸들러 매핑과 어댑터의 우선순위

```
Handler Mapping
 0 = RequestMappingHandlerMapping: @RequestMapping에서 사용
 1 = BeanNameUrlHandlerMapping: 스프링 빈 이름으로 핸들러 찾음

Handler Adaptor
 0 = RequestMappingHandlerAdaptor: @RequestMapping에서 사용
 1 = HttpRequestHandlerAdaptor: HttpRequestHandler 처리
 2 = SimpleControllerHandlerAdaptor: Controller 인터페이스 처리
```

1. **적절한 핸들러 매핑 찾아 핸들러 조회**
   1. `HandlerMapping` 을 순서대로 조회해서, 핸들러를 찾는다.
   2. 빈 이름으로 등록한 경우 RequestMappingHandlerMapping에서는 찾을 수 없어서 넘어가고,  `BeanNameUrlHandlerMapping`를 찾아서 실행한다.
   3. 핸들러를 반환한다.
2. **핸들러 어댑터 조회**
   1. `HandlerAdaptor` 의 `supports(핸들러)` 를 순서대로 호출한다.
   2. `Controller` 인터페이스를 구현한 경우 `SimpleControllerHandlerAdaptor`를 반환한다.
3. **핸들러 어댑터 실행**
   1. 디스패처 서블릿이 조회한 `SimpleControllerHandlerAdaptor` 를 실행하면서 핸들러 정보도 함께 넘겨준다.
   2. `SimpleControllerHandlerAdaptor` 는 핸들러를 내부에서 실행하고, 그 결과를 반환한다.

<br/>

### 구현으로 살펴보기

Case1: 핸들러 매핑은 `2. 빈` 핸들러 어댑터는 `3. SimpleControllerHandlerAdaptor` 로 한 경우

```java
@Component("/springmvc/old-controller")
public class OldController implements Controller {
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("OldController.handleRequest");
    return null;
  }
}
```

Case2: 핸들러 매핑은 `2. 빈` 핸들러 어댑터는 `2. HttpRequestHandlerAdaptor` 로 한 경우

```java
@Component("/springmvc/request-handler")
public class MyHttpRequestHandler implements HttpRequestHandler {
  @Override
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("MyHttpRequestHandler.handleRequest");
  }
}
```

void를 반환하다니... Case1보다 Case2가 더 구리다 우엑

실무에서는 거의 99.9% RequestMapping 애너테이션 사용한다.

<br/>

## View Resolver

```java
@Component("/springmvc/old-controller")
public class OldController implements Controller {
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("OldController.handleRequest");
    return new ModelAndView("new-form");
  }
}
```

위와 같이 논리경로만 반환하면 에러가 나기 때문에 뷰 리졸버를 만들어줘야 한다.

<br/>

### InternalResourceViewResolver

application.properties 에 다음과 같이 적어보자

```
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

스프링 부트는 `InternalResourceViewResolver` 라는 뷰 리졸버를 자동으로 등록하는데, 등록하는 시점에 application.properties에 적은 `spring.mvc.view.prefix` 와 `spring.mvc.view.suffix` 설정 정보를 같이 등록한다.

마치 아래처럼 등록한 것과 같다.

```java
@Bean
InternalResourceViewResolver internalResourceViewResolver() {
  return new InternalResourceViewResolver("/WEB-INF/views/", ".jsp");
}
```

<br/>

### 스프링 부트가 자동 등록하는 뷰 리졸버

```
1 = BeanNameViewResolver: 빈 이름으로 뷰를 찾아서 반환
2 = InternalResourceViewResolver: JSP를 처리할 수 있는 뷰를 반환
```

1. `new-form` 이라는 뷰 이름으로 viewResovler를 순서대로 호출한다.
2. `BeanNameViewResolver`는 `new-form` 이라는 스프링 빈으로 등록된 뷰가 없어서 패스된다.
3. `InternalResourceViewResolver`가 호출된다.
   - 이 뷰 리졸버는 `InternalResourceVeiw`를 반환한다.
     - 이 View는 `.render()` 하면 forward()를 호출할 수 있는 기능이 포함된다.

다른 View는 `render()`시에 실제 뷰를 렌더링하지만, JSP의 경우에는 `forward()` 를 통해서 해당 JSP가 실행되어야 렌더링이 된다.

타임리프 뷰 리졸버와 티임리프 뷰도 따로 있다.

InternalResource의 뜻: 내부에서 자원이 이동하는거. Servlet 에서 JSP로 이동했던 것처럼