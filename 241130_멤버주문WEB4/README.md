# 멤버주문 WEB 서비스 #4 | Front Controller 적용하기

MVC 패턴을 적용하면서 아쉬웠던 점은 반복되는 코드가 많다는 점이다.

프론트 컨트롤러 패턴을 도입해서 

오늘은 김영한님 스프링 MVC 강의의 "MVC 프레임워크 만들기" 의 흐름을 거의 따라갔다.

아래의 흐름으로 발전해 나갈 것이다.

1. 우리가 작성한 컨트롤러의 요청을 중앙에서 호출해주는 front controller 만들기(**전략 패턴**)
2. dispatcher forward를 공통처리 하기 위해 반환 객체 따로 만들기
3. Servlet이 전달하는 req, res를 감추도록 paramMap을 따로 넘겨주도록 변경하는데, 그러면 req.setAttribute가 안되니깐 2번에서 반환하는 객체가 Model을 담도록 변경하기
4. 3번과는 아예 다른 방식으로 뷰의 이름만 String 으로 반환하게 하고, Model에 담을건 파라미터로 넘겨받도록 하기
5. 3번과 4번을 url에 따라 원하는 컨트롤러를 호출할 수 있도록 하는 HandlerAdaptor를 만들기 (**어댑터 패턴**)

<br />

### 목차

- [ver1: Front Controller 만들기](#ver1-front-controller-만들기)
- [ver2: View 전용 객체 만들기](#ver2-view-전용-객체-만들기)
- [ver3: Servlet 감추고, ModelView 반환](#ver3-servlet-감추고-modelview-반환)
- [ver4: 단순하고 실용적인 컨트롤러](#ver4-단순하고-실용적인-컨트롤러)
- [ver5: 유연한 프론트 컨트롤러](#ver5-유연한-프론트-컨트롤러)
- [마치며](#마치며)


<br/>



### ver1: Front Controller 만들기

[[ver1 코드 보러 가기]](https://github.com/yeongbinim/TIL/commit/43f6f39dc700996405b4655a44d92a7ea195d3fd)

<div align="center"><img width="350" alt="스크린샷 2024-12-01 오후 11 24 25" src="https://github.com/user-attachments/assets/3938bc47-4f67-4317-8ff8-549db9929cd9"></div>

기존에 요청이 오면 요청에 알맞은 서블릿 구현체를 직접 호출하는 방식에서 어떤 요청이 오던 하나의 서플릿 구현체가 호출되도록 하고, 그것이 매핑 정보를 찾아서 원하는 컨트롤러를 호출하도록 변경할 것이다.

```java
@WebServlet(name = "frontControllerServlet", urlPatterns = "/front-controller/*") //주목!
public class FrontControllerServlet extends HttpServlet {
  private Map<String, Controller> controllerMap = new HashMap<>();

  public FrontControllerServlet() {
    controllerMap.put("/front-controller/members/new-form", new MemberFormController());
    controllerMap.put("/front-controller/members/save", new MemberSaveController());
    controllerMap.put("/front-controller/members", new MemberListController());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    Controller controller = controllerMap.get(requestURI);
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    controller.process(request, response);
  }
}
```

프론트 컨트롤러를 위와 같이 만들었다. 

urlPatterns = `/front-controller/*` 를 통해서  `/front-controller` 를 포함한 하위 모든 요청은 이 서블릿에서 받아들이도록 설정했다.

아쉬운 점이 있는데, 아래의 컨트롤러 구현체를 보자

```java
public class MemberListController implements Controller {
  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Member> members = memberRepository.findAll();
    request.setAttribute("members", members);
    String viewPath = "/WEB-INF/views/members.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }
}
```

모든 컨트롤러에서 뷰로 이동하는 부분에 **중복**이 있다.

`dispatcher` 를 사용하는 부분도 front controller 가 하도록 변경해보자

<br/>

### ver2: View 전용 객체 만들기

[[ver2 변경사항 보기]](https://github.com/yeongbinim/TIL/commit/c831b5bd02d559befed04649865324c6a3b9d010)

<div align="center"><img width="350" alt="스크린샷 2024-12-01 오후 11 24 37" src="https://github.com/user-attachments/assets/e22c22e3-3b46-424f-896e-2b942e3a447a"></div>

Controller가 직접 dispatcher forward 메서드를 호출하는 것이 아닌, MyView 객체를 반환하도록 변경할 것이다. 

```java
public class MyView {
  private String viewPath;
  public MyView(String viewPath) {
    this.viewPath = viewPath;
  }
  public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }
}
```

MyView 객체는 위와같이 생겼다. Controller가 이 MyView 객체에 viewPath를 담아서 주면,  FrontController에서 이 MyView 객체의 render를 대신 호출해준다.

```java
public class FrontControllerServlet extends HttpServlet {
  // 중략

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    Controller controller = controllerMap.get(requestURI);
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    MyView view = controller.process(request, response); //여기 추가
    view.render(request, response);
  }
}
```

controller를 호출해서 받은 view를 다음과 같이 호출해 주면 되는 것이다.

이제 뷰는 MyView의 책임으로 넘어갔으니 Controller가 아래처럼 가벼워 진 것을 확인할 수 있다.

```java
public class MemberListController implements Controller {
  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response){
    List<Member> members = memberRepository.findAll();
    request.setAttribute("members", members);
    return new MyView("/WEB-INF/views/members.jsp");
  }
}
```

하지만 아직 아쉬운 점이 있다.

1. HttpServletRequest와 HttpServletResponse 타입을 그대로 받는다는 점이 아쉽다. req과 res에는 많은 기능들이 들어 있으며 Controller는 이게 서블릿이 호출되는지도 모르게 하고 싶다.
2. `/WEB-INF/views` 라는 위치를 모든 컨트롤러에서 각각 명시하는게 아쉽다. 확장자도..



Map으로 대신 넘기도록 하면 지금 구조에서는 컨트롤러가 서블릿 기술을 몰라도 동작할 수 있다. 그리고 request 객체를 Model로 사용하는 대신에 별도의 Model 객체를 만들어서 반환하면 된다.

뷰 경로의 중복은 프론트 컨트롤러에서 처리하도록 변경할 것이다.

<br/>

### ver3: Servlet 감추고, ModelView 반환

[[ver3 변경사항 보기]](https://github.com/yeongbinim/TIL/commit/487ec337167c283eac836744525ae3f4824b5915)


<div align="center"><img width="350" alt="스크린샷 2024-12-01 오후 11 24 49" src="https://github.com/user-attachments/assets/699b8beb-9248-4bd9-95ed-cab4c8c145b0"></div>

Controller는 Servlet Request, Response 대신 Map을 넘겨받아서 요청 정보를 읽는데,

이때 Model이 담긴 ModelView라는 객체를 반환하고,

FrontController는 viewResolver 라는 함수를 호출하여 뷰 경로까지 합쳐진 MyView 객체로 변환하도록 할 것이다.

```java
public class ModelView {
    private String viewName;
    private Map<String, Object> model = new HashMap<>();

    public ModelView(String viewName) {
        this.viewName = viewName;
    }

    // 이하는 viewName과 model의 getter setter
}
```

우선 위와 같은 ModelView 객체를 만들어서 아래처럼 컨트롤러에서 반환하도록 했다.

```java
public class MemberListControllerV3 implements ControllerV3 { //V5를 위한 V3 네이밍
  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public ModelView process(Map<String, String> paramMap) {
    List<Member> members = memberRepository.findAll();
    ModelView mv = new ModelView("members");
    mv.getModel().put("members", members);
    return mv;
  }
}
```

그리고 FrontControllerServlet의 변경사항이 꽤 많은데 아래와 같다.

```java
public class FrontControllerServlet extends HttpServlet {
  // 중략

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    Controller controller = controllerMap.get(requestURI);
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    //변경된 부분
    Map<String, String> paramMap = createParamMap(request);
    ModelView mv = controller.process(paramMap);
    String viewName = mv.getViewName();
    MyView view = viewResolver(viewName);
    view.render(mv.getModel(), request, response);
  }
  
  private Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();
    request.getParameterNames().asIterator()
      .forEachRemaining(pName -> paramMap.put(paramName, request.getParameter(pName)));
    return paramMap;
  }

  private MyView viewResolver(String viewName) {
    return new MyView("/WEB-INF/views/" + viewName + ".jsp");
  }
}
```

- request로부터 파라미터만 추출하도록 하는 함수를 따로 만들었고, 논리경로로부터 실제 경로를 만들어 MyView 객체를 반환하는  viewResovler 함수를 작성했다.

- service 내의 변경된 부분을 보면, 이 paramMap을 컨트롤러 호출 시에 넘기고 ModelView로부터 ViewName을 받아와 viewResolver를 통해 MyView 객체를 가져와서 render한다.

`view.render()` 메서드를 보면 ver2와 다르게 매개변수가 하나 더 많은 것을 알 수 있다. 아래처럼 MyView도 변경했다.

```java
public class MyView {
  // 중략 (생성자 및 기존 render 함수)

  public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    modelToRequestAttribute(model, request);
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }

  private void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
    model.forEach((key, value) -> request.setAttribute(key, value));
  }
}
```

전달받은 Map 객체로부터 다시 request에 setAttribute를 하도록 한 것이다.

여기서 뭔가 더 변경해보고 싶은 건.. 문자열만 반환해도 해당 view를 찾을 수 있도록 바꿔보고 싶다는 것이다.

그럼 ModelView에 담기던 Model들은...? 어떡하지?

결국 파라미터를 하나 더 받아서 거기에 담도록 해야 겠다.



<br/>

### ver4: 단순하고 실용적인 컨트롤러

[[ver4 변경사항 보기]](https://github.com/yeongbinim/TIL/commit/276c8cd5d5ceff33099f4bed19d059eaffaa60ff)

<div align="center"><img width="350" alt="스크린샷 2024-12-01 오후 11 25 03" src="https://github.com/user-attachments/assets/d1b3d628-8e67-43ee-a81c-d4c9516743fb"></div>

ver3 처럼 ModelView가 아닌 그냥 ViewName만 반환하고, 대신 model을 담을 Map을 파라미터로 받도록 할 것이다.

FrontControllerServlet에서 service 변경사항만 확인해보자

```java
@Override
protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  String requestURI = request.getRequestURI();
  Controller controller = controllerMap.get(requestURI);
  if (controller == null) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    return;
  }
  //변경된 부분
  Map<String, Object> model = new HashMap<>();
  Map<String, String> paramMap = createParamMap(request, model);
  String viewName = controller.process(paramMap);
  MyView view = viewResolver(viewName);
  view.render(model, request, response);
}
```

나는 이게 ver3보다 훨씬 더 맘에 든다.

하지만 다른 개발자는 Controller를 구현할 때 V3방식으로 구현하고 싶다고 하면 어떡하지?

컨트롤러 개발자가 여러 컨트롤러 구현 방식으로 구현해도 허용하도록 해보자

<br/>

### ver5: 유연한 프론트 컨트롤러

[[ver5 변경사항 보기]](https://github.com/yeongbinim/TIL/commit/15ee0cd48bbe88e3c37ea105a8cc33ff068a6395)

<div align="center"><img width="350" alt="스크린샷 2024-12-01 오후 11 25 18" src="https://github.com/user-attachments/assets/873f741c-9868-4d7c-8b0b-cf299336b48e"></div>

어댑터 패턴을 이용할 건데, 매핑정보로 부터 가져온 핸들러(컨트롤러보다 더 넓은 범위의 개념, 이유는 이제 어댑터가 있기 때문에 꼭 컨트롤러가 아니어도 괜춘)를 통해 이 핸들러를 사용할 수 있는 어댑터를 어댑터 목록에서 찾는다.

그렇게 찾은 어댑터를 통해 어댑터에 핸들러를 넘겨서 호출시키면, 어댑터는 무조건 ModelView를 반환하도록 하고, 그 이후 로직은 이전과 같다.

<br/>

핸들러 어댑터 인터페이스 부터 살펴보자

```java
public interface HandlerAdaptor {
    boolean supports(Object handler);
    ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException;
}
```

특정 handler를 위한 어댑터인지 확인할 수 있도록 supports 라는 메서드를 제공해주고, 요청받은 핸들러를 호출할 수 있도록 handle 메서드를 제공한다.

V3, V4 구현체를 살펴보면 아래와 같다.

```java
public class ControllerV3HandlerAdaptor implements HandlerAdaptor {
  @Override
  public boolean supports(Object handler) {
    return (handler instanceof ControllerV3);
  }

  @Override
  public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    ControllerV3 controller = (ControllerV3) handler;
    Map<String, String> paramMap = createParamMap(request);
    return controller.process(paramMap);
  }
}

public class ControllerV4HandlerAdaptor implements HandlerAdaptor {
  @Override
  public boolean supports(Object handler) {
    return (handler instanceof ControllerV4);
  }

  @Override
  public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    ControllerV4 controller = (ControllerV4) handler;
    Map<String, String> paramMap = createParamMap(request);
    Map<String, Object> model = new HashMap<>();
    //여기 중요
    String viewName = controller.process(paramMap, model);
    ModelView mv = new ModelView(viewName);
    mv.setModel(model);
    return mv;
  }
}
```

중요! 라고 적은 저 부분!! 어댑터에서 이 부분이 단순하지만 중요한 부분이다.

어댑터가 호출하는 `ControllerV4` 는 뷰의 이름을 반환한다. 그런데 어댑터는 뷰의 이름이 아니라 `ModelView` 를 만들어서 반환해야 한다. 여기서 어댑터가 꼭 필요한 이유가 나온다. 

`ControllerV4` 는 뷰의 이름을 반환했지만, 어댑터는 이것을 ModelView로 만들어서 형식을 맞추어 반환한다. 마치 110v 전기 콘센트를 220v 전기 콘센트로 변경하듯이!

<br/>

createParamMap은 이전과 동일해서 생략했다. frontControllerServlet에 있었던 메서드가 이 어댑터로 이동됐다는 것만 확인하자

최종 FrontControllerServlet 코드는 아래와 같다.

```java
@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {
  private final Map<String, Object> handlerMappingMap = new HashMap<>();
  private final List<HandlerAdaptor> handlerAdaptors = new ArrayList<>();

  public FrontControllerServletV5() {
    initHandlerMappingMap();
    initHandlerAdaptors();
  }

  private void initHandlerMappingMap() { //추가
    handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

    handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
  }

  private void initHandlerAdaptors() { //추가
    handlerAdaptors.add(new ControllerV3HandlerAdaptor());
    handlerAdaptors.add(new ControllerV4HandlerAdaptor());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    Object handler = handlerMappingMap.get(requestURI);
    if (handler == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    HandlerAdaptor adaptor = getHandlerAdaptor(handler);
    ModelView mv = adaptor.handle(request, response, handler);
    MyView view = viewResolver(mv.getViewName());
    view.render(mv.getModel(), request, response);
  }

  private HandlerAdaptor getHandlerAdaptor(Object handler) { //추가
    for (HandlerAdaptor adaptor : handlerAdaptors) {
      if (adaptor.supports(handler)) {
        return adaptor;
      }
    }
    throw new IllegalArgumentException("handler adaptor를 찾을 수 없습니다. handler=" + handler);
  }
}
```

getHandlerAdaptor메서드를 통해 handlerAdaptors 중에 이 핸들러를 지원하는 어댑터인지 supports 메서드를 통해 찾고, 지원되는 것을 반환하도록 한다.



### 마치며

디스패처 서블릿은 기술적인 부분보다는 패턴이나 객체지향과 같은 구조적인 부분이 강해서 내 멋대로 개발하면 안될거 같다는 느낌이 들어 김영한님 강의를 대부분 따라 갔다.

어댑터 패턴이라는 것에 대해서 장점을 정말 제대로 느꼈고, 디스패처 서블릿이 정말 스프링 MVC의 중심이라고 할 만큼 많은 일을 담당하고 있는 존재라는 것을 알았다.

Controller를 개발하는 입장에서 어떻게 더 편하게 Controller를 작성할 수 있는지 느낄 수 있는 순간이다.

이 Controller는 어떻게 저 핸들러 매핑에 주입이 되게 될까? 그것에 대해서는 한 번 구현해봐도 좋을거 같기는 하다.





