# 회원 주문 CLI 서비스 #3

이전 시리즈: [회원 주문 CLI 서비스 #1 ](../241115_멤버주문앱1), [회원 주문 CLI 서비스 #2 ](../241118_멤버주문앱2)

현재까지 만든 ApplicationContext에서 살짝 아쉬운 부분이 있다.

그건 의존관계를 형성하고 싶은 클래스 (줄여서 빈이라고 하겠다)가 추가될 때 마다 아래와 같이 AppConfig에 추가해야 된다.

```java
@Configuration
public class AppConfig {
  @Bean
  public NewComponent newComponent() {
    return new NewComponent(
    	의존 관계들
    );
  }
}
```

설정하고 싶은 빈이 수십개, 아니 수백개가 되면 어떻게 될까?

<br/>

NestJS는 각 도메인마다 `module.ts` 라는 파일에 이것들을 관리했던 거 같다.

즉, 각 도메인 별로 AppConfig를 만드는 것이다.

하지만 NestJS를 하면서 가장 불편했던 점이 이것이었다.

<br/>

**왜 매번 연결해줘야되지?** 어차피 Controller는 Service 필요하고, Service는 Repository 필요한거 뻔한데?

이렇게 **업무로직처럼 뻔한건 자동으로 연결**되고, 정말 관계들이 필요한 **기술지원 클래스들만 의존관계를 명시적으로 연결**하도록 하고 싶었다.

따라서 아래의 요구사항을 세웠다.

- 기능 추가
  - [x] `@Component` 애너테이션이 있는 클래스는 모두 싱글턴 컨테이너에 관리하기
    - 이때 `@ComponentScan` 이 있는 클래스의 하위 패키지들만 포함된다.
  - [x] `@Autowired` 애너테이션이 있는 생성자의 매개변수 타입에 맞게 의존관계 주입해 주기

그런데, 앞서 ApplicationContext를 만들면서 ComponentScanner와 연관지으며 반복되는 것들을 가볍게 할 수 있을 것 같았고, 현재 jar를 그대로 실행할때에는 안되는 상태라 다음과 같이 리팩터링 요구사항을 사전에 먼저 하기로 했다.

- 리팩터링
  - [x] main에서 컨트롤러 실행 매퍼를 관리하지 말고, CommandMapping이라는 곳에서 관리할 것
  - [x] ComponentScanner가 `jar` 에서도 정상적으로 실행되게 할 것



[[전체 코드 보러가기]](./src)

<br/>

## 목차

- [리팩터링](#리팩터링)
  - [CommandMapping 분리](#commandmapping-분리)
  - [jar에서는 안됨](#jar에서는-안됨)
- [기능추가: 자동 주입 컨테이너](#기능추가-자동-주입-컨테이너)
  - [@Component와 @ComponentScan](#component와-componentscan)
  - [@Autowired로 자동 주입](#autowired로-자동-주입)
- [마치며](#마치며)



<br/>

## 리팩터링

가장 먼저 메서드들을 관리하는 CommandMapping을 분리하여 main을 가볍게 하고,

그 후에 jar에서는 현재 코드가 돌아가지 않는 원인을 분석해보며 개선을 할 것이다.

<br/>

### CommandMapping 분리

main 함수에 있던 initializeMapping과 execute 메서드를 갖는 CommandMappingHandler를 분리하였다.

즉, 메서드 매퍼를 관리하는 책임을 갖는 것이다.

```java
public class CommandMappingHandler {
  private final ApplicationContext context;
  private final ComponentScanner scanner = ComponentScanner.getInstance();
  private final Map<String, Method> methodMap = new HashMap<>();

  public CommandMappingHandler(ApplicationContext context) {
    this.context = context;
    initializeMapping();
  }

  private void initializeMapping()  {
    List<Class<?>> classList = scanner.componentScan(Controller.class);
    for (Class<?> clazz : classList) {
      String basePath = "/";
      if (clazz.isAnnotationPresent(CommandMapping.class)) {
        basePath = clazz.getAnnotation(CommandMapping.class).value();
      }
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(CommandMapping.class)) {
          methodMap.put(메서드 등록);
        }
      }
    }
  }

  public void execute(String command) throws ReflectiveOperationException {
    기존 execute 함수
  }
}
```

위와 같이 작성을 하고, 기존에 instanceMap은 이제 내가 만든 ApplicationContext를 사용하도록 했다.

아래는 이에따라 가벼워진 main 메서드이다.

```java
public class MemberOrderApp {
  public static void main(String[] args) {
    CommandMappingHandler commandMappingHandler = new CommandMappingHandler(new ApplicationContext(AutoAppConfig.class));
    try (Scanner scanner = new Scanner(System.in)) {
      while (true) {
        System.out.print("호출하고 싶은 메서드를 입력하세요 (종료는 exit): ");
        String command = scanner.nextLine();
        if ("exit".equalsIgnoreCase(command)) {
          break;
        }
        commandMappingHandler.execute(command);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
```



<br/>

### jar에서도 호환

아래는 이전에 클래스 로더를 생성하여 클래스 파일을 로드하는 코드이다.

```java
File dir = new File("build/classes/java/main/" + path);
List<Class<?>> components = new ArrayList<>();
URL[] urls = {dir.toURI().toURL()};
URLClassLoader loader = new URLClassLoader(urls);
File[] files = dir.listFiles();

File file = files[0]; // 코드 설명을 위한 임시 코드
String className = file.getName().substring(0, file.getName().length() - 6);
Class<?> clazz = loader.loadClass(packageName + "." + className);
```

URLClassLoader를 생성하기 위해서 URL 타입의 클래스 파일들이 저장된 경로가 필요했고, 그러기 위해서 `new File(경로)`로 File 객체를 생성했다.

여기서 이 파일 경로 지정 부분이 문제가 됐다.

CLI에서 JAR를 직접 실행할 때에는 JAR파일 내부나 다른 클래스패스 경로에서 클래스를 로드하기 때문에, 파일 시스템 경로를 직접 사용하는 것이 적합하지 않았던 것이다.

구체적으로는 `dir.listFiles()` 호출에서 dir이 실제 파일 시스템상에 존재하지 않아 null을 반환하고 있어서 Null Pointer Exception이 나게 되었다.

<img width="1200" alt="스크린샷 2024-11-20 오전 10 00 41" src="https://github.com/user-attachments/assets/f722ab78-7eba-4712-b678-4b61d9fa9754">

우선 기존 방식을 먼저 바꾸기로 했다.

URLClassLoader로 클래스를 loader.loadClass()하는 방식은 현재 JVM상에 올려져있는 클래스들을 사용하는 것이 아니라, class파일에서 직접 클래스 로더로 새롭게 로드하는 것이기 때문이었다.

```java
ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
Enumeration<URL> resources = classLoader.getResources(packageName);
```

위와같이 현재 사용중인 클래스로더로부터 resource를 받아올 수가 있는데, 이 resource로부터 클래스 이름을 알아내서 `Class.forName(className)`하면 **JVM상에 올려진 클래스를 사용할 수 있다**.

이 메커니즘을 이용하여 코드를 아래와 같이 변경하였다.

```java
private void init() {
  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
  Enumeration<URL> resources = classLoader.getResources(packageName);
  while (resources.hasMoreElements()) {
    processResource(resources.nextElement(), packageName, classLoader);
  }
}

private void processResource(URL resource, String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
  if (resource.getProtocol().equals("jar")) {
    // jar 처리
  } else {
    // 파일 처리
  }
}
```

<br/>


## 기능추가: 자동 주입 컨테이너

현재 ApplicationContext이 생성될때 `@Configuration` 이 있는 애너테이션을 처리하는 registerConfiguration 함수가 호출되고 있는데, 이어서 autoConfiguration()이라는 메서드를 만들어 이것이 호출 되도록 할 것이다.

```java
 public ApplicationContext(Class<?> configClass) {
  this.registerConfiguration(configClass);
  this.autoConfiguration(configClass);
}
```

저 autoConfiguration을 만들어 볼 것이다.

[[자동 주입 컨테이너 기능 추가 코드 보러가기]](https://github.com/yeongbinim/TIL/commit/bd9ee63063a860467ee23aa76363528213a39534)

<br/>

### @Component와 @ComponentScan

우선 두 애너테이션을 만든다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {
}
```

클래스에 붙일 애들이기 때문에, 위와 같이 ElementType.TYPE으로 지정했다.

이걸 사용해서 아래와 같은 로직을 추가했다.

```java
private final ComponentScanner scanner = ComponentScanner.getInstance();

private void autoConfiguration(Class<?> configClass) {
  if (configClass.isAnnotationPresent(ComponentScan.class)) {
    List<Class<?>> components = scanner.componentScanSubPackages(configClass, Component.class);
    for (Class<?> component : components) {
	    //앞으로 이 components들을 처리하는 로직 작성
    }
  }
}
```

`@ComponentScan` 애너테이션이 있는지 확인하고, 우리가 만든 componentScanner로부터 해당 클래스가 있는 패키지를 포함하여 하위 패키지들로부터 컴포넌트들을 불러온다.

componentScanner.componentScanSubPackages는 아래와 같이 작성했다.

```java
private final List<Class<?>> components;

public List<Class<?>> componentScanSubPackages(Class<?> baseClass, Class<? extends Annotation> annotation) {
  String basePackage = baseClass.getPackage().getName();
  return components.stream()
    .filter(clazz -> clazz.getPackage().getName().startsWith(basePackage))
    .filter(clazz -> clazz.isAnnotationPresent(annotation))
    .toList();
}

```

이미 모든 클래스들을 읽어왔는데, 요청이 올때마다 클래스들을 읽어오는 건 오버헤드가 클 거라 판단되어, 메모리에 모든 컴포넌트들을 갖고 있고, 요청이 올때 쉽게 반환하도록 구성했다.

stream을 통해 간결하게 작성했다.

<br/>

### @Autowired로 자동 주입

`@Autowired` 애너테이션을 아래와 같이 만들었다.

```java
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
```

지금은 생성자에만 이 기능을 추가했지만, 필드 주입이나, setter 주입도 원래는 되는게 맞기에 일단은 3가지 모두 허용되게 하긴 했다.

autoConfiguration을 이어서 작성해보자

```java
private final Map<String, Object> singletonBeans = new HashMap<>();
private final ComponentScanner scanner = ComponentScanner.getInstance();

private void autoConfiguration(Class<?> configClass) {
  if (configClass.isAnnotationPresent(ComponentScan.class)) {
    List<Class<?>> components = scanner.componentScanSubPackages(configClass, Component.class);
    for (Class<?> component : components) {
	    String beanName = component.getSimpleName();
      if (!singletonBeans.containsKey(beanName)) {
        Constructor<?> constructor = component.getDeclaredConstructor();
        if (constructor.isAnnotationPresent(Autowired.class)) {
          Object beanInstance = createBeanInstance(constructor, components);
          singletonBeans.put(beanName, beanInstance);
        }
      }
    }
  }
}
```

componentScanner로 부터 불러온 컴포넌트들을 순회하며 생성자를 불러온다.

생성자는 그냥 호출하면 안되고, createBeanInstance 라는 함수를통해 매개변수를 주입시켜주도록 했다.

createBeanInstance는 딱 봐도 재귀적으로 할 수 밖에 없었다. Controller생성자는 Service인스턴스가 필요하고, Service생성자를 보면 Repository인스턴스가 필요할 테니깐..

이 엄청난 createBeanInstance()를 한번 작성해보자. 아래는 의사 코드가 섞여있는 코드이다.

```java
private Object createBeanInstance(Constructor<?> constructor, List<Class<?>> components) {
  Class<?>[] parameterTypes = constructor.getParameterTypes(); //파라미터 불러와서
  Object[] initArgs = new Object[parameterTypes.length]; // 초기화 인자들을 저장할 배열 생성
  for (int i = 0; i < parameterTypes.length; i++) { //파라미터 순회
    빈이름 = 현재 파라미터 타입이 인터페이스라면 ? 이 인터페이스를 구현하고 있는 컴포넌트 이름 : 아니면 현재 타입의 이름
    빈이름이 singletonBeans 맵에 들어있다면 ? initArgs[i]에 그 빈을 넣고 continue;
      : 없으면 직접 인스턴스 생성해줘야 하는데, 생성자의 파라미터가 하나 이상 있다면 ? 재귀적으로 createBeanInstance를 재호출
        : 파라미터가 없으면 직접 생성해서 singletonBeans.put하고, initArgs[i]에 그 빈을 넣는다.
  }
  return constructor.newInstance(initArgs);
}
```

for문 안의 저 의사코드에 해당하는 실제 코드가 40줄이다... 머리가 잘 안굴러가서 작성만 4시간 넘게 했다.. 자세한 코드는 [[변경된 ApplicationContext 코드]](https://github.com/yeongbinim/TIL/commit/bd9ee63063a860467ee23aa76363528213a39534#diff-fd6d1ef61fd278a632d2bfbd48268a1337c210d044e58ff816392c7079ead470R23) 여기서 확인하자

<br/>

## 마치며

마지막 createBeanInstance 재귀문을 작성하는 부분에서.. 진짜 엄청난 고생을 했다.

몇시간에 걸쳐 머리를 쥐어짜내면서 결국 해냈을때에는 정말 혼자서 박수를 마구 치면서 스스로를 엄청나게 칭찬해 주었다.

4~5일에 걸친 "스프링 없는 회원 주문 CLI 서비스" 가 끝이 났다.

**혼자 요구사항을 세우고, 그 요구사항 들을 하나씩 체크해나가는 과정이 나를 몰입**하게 만드는 것 같다.

강의만 그냥 듣는것보다 이렇게 요구사항을 해결하기 위해 강의를 찾게 되면... 지루할 수가 없다.

<br/>

다음 시리즈는 아마도 Spring MVC가 어떻게 지금의 구조를 갖게 되었는지를 이해하기 위해

WebServer를 따로 띄우고 CGI스크립트를 따로 작성하는 것부터.. 발전시키려 하는데, 사실 시작 안해서 감도 못잡긴 하겠다.

<br/>

아 그 전에 테스트 코드 작성하는 것부터 할 것같다.

지금 내가 작성한 코드가 모두 레거시가 될 것이라고 확신했던게, 어떤게 정상적으로 돌아가는지 직접 읽어서 확인해야한다는 점에서였다.

코드가 정상적으로 돌아간다는 것이 확인 되어야 레거시 코드가 아닌 의미있는 코드가 된다고 생각하기 때문에,

테스트 작성하는 연습을 지금부터 해볼 것이다.
