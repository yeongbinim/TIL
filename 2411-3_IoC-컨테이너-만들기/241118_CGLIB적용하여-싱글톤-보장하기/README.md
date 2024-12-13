# CGLIB적용하여 싱글톤 보장하기

<details>
<summary>지난 요구사항</summary>
<ul>
  <li>기술 요구사항
    <ul>
      <li>CLI 환경에서 사용자로부터 입력을 받아 기능 요구사항을 구현한다.</li>
      <li>저장을 위해 자체 메모리를 사용할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)</li>
    </ul>
  </li>
  <li>기능 요구사항
    <ul>
      <li>회원이 추가되고, 모든 회원이 조회될 수 있다.
        <ul>
          <li>회원은 NORMAL과 VIP 두 가지 등급이 있다.</li>
        </ul>
      </li>
      <li>특정 회원의 주문이 추가되고, 특정 회원의 주문들이 조회될 수 있다.
        <ul>
          <li>회원 등급에 따라 할인 정책을 적용할 수 있다.</li>
          <li>할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용한다. (나중에 변경 될 수 있다.)</li>
          <li>할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다.</li>
        </ul>
      </li>
    </ul>
  </li>
</ul>
</details>

위 요구사항에 대해 구현한 [지난 실습](../241115_3계층-Architecture와-Dto활용하여-구조잡기)에서 아쉬운 부분은 다음과 같다.

```java
public class OrderServiceImpl implements OrderService {
  private OrderRepository orderRepository = new MemOrderRepository();
  private MemberRepository memberRepository = new MemMemberRepository();
  private DiscountPolicy discountPolicy = new FixDiscountPolicy();
}
```

- 10% 비율 금액 할인으로 바꾸려 하는데, 현재 아래와 같이 코드가 작성되어 있어서 정책을 바꾸면 위의 코드가 같이 변경해야 한다.

- 저장도 MySQL이라는 외부 DB로 바꾸려 하는데, 마찬가지로 코드가 같이 변경된다.



즉, DIP원칙 (인터페이스에 의존한다) 을 지키지 않아서 OCP원칙 (확장에 열려있고 변경에는 닫혀있다)이 지켜지지 않은 것이다.

그리고, 이 클래스는 내가 **어떤 구현 클래스를 사용할 건지에 관심을 가짐과 동시에 내부 기능 구현에도 신경써야 하는 점**에서 어떻게 보면 SRP(단일 책임) 원칙을 지키지 않은 것이라 볼 수 있다.

이걸 해결해보기 위해 다음의 요구사항을 세웠다.

- 리팩터링
  - [x] 의존관계는 AppConfig 클래스에서만 관리
- 기능추가
  - [x] AppConfig를 리플렉션하여 의존관계대로 주입된 인스턴스를 ApplicationContext 관리하도록 하기
  - [x] ApplicationContext는 하나의 인스턴스만 사용하는 걸 보장할 것

[[전체 코드 보러 가기]](../241115_3계층-Architecture와-Dto활용하여-구조잡기/src/)

<br/>

## 목차

- [리팩터링: 관계 지어주는 관심사 클래스 분리하기](#리팩터링-관계-지어주는-관심사-클래스-분리하기)
  - [DIP원칙을 지키도록 변경](#dip원칙을-지키도록-변경)
  - [AppConfig에만 의존관계 설정](#appconfig에만-의존관계-설정)
  - [구현 결과 및 개선할 점](#구현-결과-및-개선할-점)
- [기능추가: 인스턴스 관리 컨테이너](#기능추가-인스턴스-관리-컨테이너)
  - [@Bean 애너테이션 만들기](#bean-애너테이션-만들기)
  - [ApplicationContext 만들기](#applicationcontext-만들기)
  - [구현 결과 및 아쉬운 점](#구현-결과-및-아쉬운-점)
- [기능추가: CGLIB를 활용한 싱글턴 컨테이너](#기능추가-cglib를-활용한-싱글턴-컨테이너)
  - [@Configuration 애너테이션 만들기](#configuration-애너테이션-만들기)
  - [AppConfig의 메서드 가로채기](#appconfig의-메서드-가로채기)
  - [구현 결과](#구현-결과)
- [마치며](#마치며)



<br/>

## 리팩터링: 관계 지어주는 관심사 클래스 분리하기

[[변경된 코드 보러 가기]](https://github.com/yeongbinim/TIL/commit/6941597143bada9638976d8317a700284560da2a)

우선 외부로부터 인스턴스를 주입받아 할당받는 식으로 하고, 클래스는 인터페이스에만 의존하도록 변경해야 한다.

<br/>

### DIP원칙을 지키도록 변경

아래처럼 ServiceImpl이 다른 구현체들을 주입 받도록 변경할 것이다. (Controller도 Service를 주입 받도록 구현한다)

```java
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final DiscountPolicy discountPolicy;

  public OrderServiceImpl(OrderRepository orderRepository, MemberRepository memberRepository, DiscountPolicy discountPolicy) {
  this.orderRepository = orderRepository;
  this.memberRepository = memberRepository;
  this.discountPolicy = discountPolicy;
}
```

Service 클래스를 사용하려면 어떤 `Repository` | `DiscountPolicy` 구현체를 쓸지 정해줘야 하고, Controller클래스를 사용하려면 여기에 더해 어떤 `Service` 구현체를 쓸지 정해줘야 한다.

```java
OrderRepository orderRepository = new OrderRepository();
MemberRepository memberRepository = new MemberRepository();
DiscountPolicy discountPolicy = new FixDiscountPolicy();

OrderService service = new OrderServiceImpl(orderRepository, memberRepository);

OrderController controller = new OrderController(service);
```

그렇다면 매번 main측에서 이렇게 Controller 쓰려고 그 하위 인스턴스들 전부 생성해서 넣어줘야 하는가? 그건 너무 빡세다.

따라서 이제 오직 어떤 클래스가 어떤 구현 클래스를 사용할 건지 주입해주는 책임만 가진 클래스(AppConfig)를 따로 만들어서 관리하도록 구성해 볼 것이다.

<br/>

### AppConfig에만 의존관계 설정

```java
public class AppConfig {
  public MemberService memberService() {
    return new MemberServiceImpl(memberRepository());
  }
  public MemberRepository memberRepository() {
    return new MemoryMemberRepository();
  }
  public OrderService orderService() {
    return new OrderServiceImpl(
        orderRepository(),
        memberRepository(),
        discountPolicy());
  }
  public OrderRepository orderRepository() {
    return new MemoryOrderRepository();
  }
  public DiscountPolicy discountPolicy() {
    return new RateDiscountPolicy();
  }
}
```

위의 AppConfig만 봐도 객체의 의존관계들이 모두 보이며, 외부에서는 orderService를 받고 싶을때

appConfig.orderService() 이렇게만 호출하면 된다.

관심사가 명확하게 분리된 것이다.

<br/>

### 구현 결과 및 개선할 점

AppConfig에서 커다란 문제점이 하나 있다.

memberService를 호출할 때마다 새로운 인스턴스가 만들어진다는 점이다.

<div align="center"><img width="400" alt="스크린샷 2024-11-19 오전 11 05 02" src="https://github.com/user-attachments/assets/d77741be-be54-41a3-8615-2358e0513bb4"></div>

매번 인스턴스를 생성할 필요가 없는데, 이렇게 되는 건 분명히 문제가 있다.

그렇다면 모든 클래스에 싱글턴 패턴을 구현하여 .getInstance() 하도록 해야되냐?

그건.. 의미없는 코드의 양이 많아지게 된다. 그리고 테스트하기도 어려울 것 같으며, 여러 측면에서 유연성이 떨어질 것 같다.

<br/>

## 기능추가: 인스턴스 관리 컨테이너

[[변경된 코드 보러 가기]](https://github.com/yeongbinim/TIL/commit/1869daf7e7c7b0ef06effdeb0b60aeabccfbce4b)

AppConfig안에 있는 메서드들을 모두 읽어서 해당 메서드를 미리 호출시켜놓고, 클라이언트 개발자는 이 컨테이너로부터 값을 꺼내 쓰도록 하자.

<br/>

### @Bean 애너테이션 만들기

우리가 만들 컨테이너가 관리하는 객체들을 모두 빈이라고 할 것이다. 이에따라 AppConfig에서 각 메서드들에 붙일 애너테이션을 `@Bean` 이라 하겠다. 이 애너테이션을 만들어보자

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
}
```

value를 직접 정하도록 추가할 수도 있지만, 추후에 추가해보도록 하자.

이것을 사용하는 AppConfig는 아래처럼 사용한다.

```java
public class AppConfig {
  @Bean
  public MemberService memberService() {
    return new MemberServiceImpl(memberRepository());
  }
  ...
}
```

이제 Bean이 붙어있는 메서드들을 읽어서 관리하는 컨테이너를 만들어보자

<br/>

### ApplicationContext 만들기

애너테이션이 있는 메서드들을 읽기 위해서 [DynamicMethodCaller](../241114_리플렉션으로-OCP-컨트롤러-만들기)를 만들때 사용했던 리플렉션 기술이 필요하다.

구현한 코드는 다음과 같다.

```java
public class ApplicationContext {
  private final Map<String, Object> singletonBeans = new HashMap<>();

  public ApplicationContext(Class<?> configClass) { // (1)
    this.register(configClass);
  }

  private void register(Class<?> configClass) {
    try {
      Object configInstance = configClass.getDeclaredConstructor().newInstance();
      for (Method method : configClass.getDeclaredMethods()) { // (2)
        if (method.isAnnotationPresent(Bean.class)) { // (3)
          Object bean = method.invoke(configInstance); // (4)
          singletonBeans.put(method.getName(), bean); // (5)
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public Object getBean(String beanName) {
    return singletonBeans.get(beanName);
  }
}
```

1. `new ApplicationContext(AppConfig.class)`로 클래스 정보를 넘기면 

2. `configClass.getDeclaredMethods` 로 내가 선언한 메서드들을 읽어와서 순회한다
3. `method.isAnnotationPresent(Bean.class)` 로 내가 만든 @Bean 애너테이션이 있는지 확인하고
4. `method.invoke()` 로 함수를 실행한다.
5. `sigletonBeans.put(method.getName(), bean)` 으로 4번의 결과를 Map에 보관한다.



<br/>

### 구현 결과 및 아쉬운 점

여전히 문제가 존재했다. AppConfig의 memberRepository에 아래의 로그를 넣어두고 실행시켜 보자

```java
public class AppConfig {
  ...
  @Bean
  public MemberRepository memberRepository() {
    System.out.println("repository 생성");
    return new MemoryMemberRepository();
  }
  ...
}
```

<div align="center"><img width="150" alt="스크린샷 2024-11-19 오전 11 30 26" src="https://github.com/user-attachments/assets/ef6fdb43-7005-40b4-b2f8-e307579881ce"></div>

각 메서드가 한번씩만 호출된다는 것은 보장이 되지만 내부적으로 memberRepository()를 호출하는 다른 메서드들이 있기 때문에, 이런 현상이 생긴 것이다.

orderService()가 사용하는 memberRepository와 memberService()가 사용하는 memberRepository가 각각 다른 인스턴스라는 것은 분명히 문제가 된다.

분명 회원을 생성했는데, 주문할때 되니깐 없으면 안되잖아..

<br/>

## 기능추가: CGLIB를 활용한 싱글턴 컨테이너

[[변경된 코드 보러 가기]](https://github.com/yeongbinim/TIL/commit/99f4f6f3e580ff970bf2458dc23e3917c3eb9a3e)

이전에 `memberRepository()`를 호출했으면, `orderService()`가 호출될때 내부적으로 `memberRepositoy()`가 호출되는 것을 막아야한다.

즉, 메소드 호출을 가로채서 추가 로직이 수행되는 동적 프록시 기술이 필요하다.

JDK 자체에도 프록시를 런타임에 생성할 수 있는 기술이 있지만, `java.lang.reflect.Proxy` 클래스와 `java.lang.reflect.InvocationHandler` 인터페이스를 사용하여 구현되는 동적 프록시는 인터페이스 기반만 가능하다.

AppConfig에 대한 인터페이스를 따로 만들어야되나... 고민이 있었지만, 그럴경우 중복 코드가 너무 많아진다.

다른 방법을 찾아보다가 Spring 내부에서도 CGLIB를 사용한다는 것을 알게 되었고, 이것을 사용해보기로 결정했다.

[CGLIB 레포지토리](https://mvnrepository.com/artifact/cglib/cglib) 에서 3.3.0을 받아서 사용했다.

<br/>

### @Configuration 애너테이션 만들기

이 `@Configuration` 이라는 애너테이션이 있어야, 동적 프록시를 활용하여 하나의 인스턴스만 관리되도록 설정한다는 의미로 사용하려고 한다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
```

위와 같이 만들고 AppConfig위에 달아두자

```java
@Configuration
public class AppConfig {
  ...
}
```

마지막으로 ApplicationContext를 다음과 같이 수정하면 된다.

```java
public class ApplicationContext {
  ...
  private void register(Class<?> configClass) {
    try {
      Object configInstance = configClass.getDeclaredConstructor().newInstance();
      if (configClass.isAnnotationPresent(Configuration.class)) {
        configInstance = this.configure(configClass);
      }
      ...
  }
  private Object configure(Class<?> configClass) {}
  ..
}
```

이제 저 동적 프록시를 담당하는 configure를 작성하기만 하면 된다.

<br/>

### AppConfig의 메서드 가로채기

configure 코드는 아래와 같이 작성했다.

```java
private Object configure(Class<?> configClass) {
  Enhancer enhancer = new Enhancer();
  enhancer.setSuperclass(configClass); // (1)
  enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
    String methodName = method.getName();
    if (method.isAnnotationPresent(Bean.class) && singletonBeans.containsKey(methodName)) {
      return singletonBeans.get(methodName);
    }

    Object result = proxy.invokeSuper(obj, args);
    if (method.isAnnotationPresent(Bean.class)) {
      singletonBeans.put(methodName, result);
    }
    return result;
  }); // (2)

    return enhancer.create(); // (3)
}
```

1. `enhancer.setSuperclass()` 는 기반 클래스를 생성한다. 이것은 CGLIB가 생성할 새로운 클래스가 상속받을 클래스를 지정한다.
2. `enhancer.setCallback()` 은 프록시 인스턴스에서 실제 메소드 호출을 가로채는 로직을 정의한다. 여기서 사용된 MethodInterceptor는 내부적으로 메소드 호출이 발생할 때마다 실행될 콜백 인터페이스 이다.
3. `enhancer.create()` 를 하면 앞서 등록된 superclass와 callback가 반영된 새로운 클래스의 인스턴스를 생성하게 된다. 이때 생성된 Class는 기존의 Class와 다르다.



결국 setSuperclass로 기반 클래스 지정해주고, setCallback으로 프록시할때 앞뒤 로직 작성해주고, create로 새로운 인스턴스를 만들어주는 것이다.

기술 자체는 듣기만 해도 어렵지만, 이렇게 라이브러리를 사용하니 쉽게 문제를 해결했다.

<br/>

### 구현 결과

이제 다시 실행시켜보면, 'repository 생성' 로그가 한번만 호출되는 것을 확인할 수 있다.

<div align="center"><img width="325" alt="스크린샷 2024-11-19 오후 12 44 17" src="https://github.com/user-attachments/assets/306242a0-1094-42b6-9ef8-a02461036780"></div>



<br/>

## 마치며

의존관계를 어떻게 해결할지 고민해보면서 좋은 구조를 만들어보려고 하니 어쩔 수 없이 스프링을 떠올리게 되었고, 결국 스프링의 구조를 거의 그대로 따라가게 되었다.

애너테이션을 스캔하기 위해서 리플렉션을 사용하게 되었고, 싱글턴 인스턴스로 관리하기 위해서 CGLIB를 사용하게 되었다.

직접 만들어보니 왜 스프링이 리플렉션을 썼는지, CGLIB를 사용하는지 알 수 있었다.

스프링을 해부하진 않았는데 스프링의 내부 원리를 이해해 나갈 수 있었던 것 같다.

<br/>

다음 내용은 ApplicationContext, ComponentScanner를 잘 리팩터링 하고, 아마 AppConfig없이 자동으로 ComponentScan되고 Autowired 되는 기능을 추가해 볼 것이다.
