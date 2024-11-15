# 회원 주문 CLI 서비스

계산기 프로그램을 구현하다가 문득

'콘솔에서 사용자 입력을 받아서 DB에 직접 저장하는 서비스를 만들 면 어떨까?'

즉, CLIController를 만드는 것이다.

스프링의 경우에도 API를 요청받고 응답하는 RestController와 타임리프로 SSR하게끔 html을 반환하는 일반 Controller의 작성 방식이 다르지만,

Service나 Repository 코드는 유지가 된다.

<br/>

어제 내가 만들어 둔 [DynamicMethodController]()를 활용하여, 

Controller, Service, Repository **세 계층간의 의존 관계 관리에만 집중**적으로 고민하여,

스프링이 어떻게 의존관리를 하는지에 대한 간단한 원리를 이해해보려고 요구사항을 아래와 같이 만들었다.



- 기술 요구사항
  - CLI 환경에서 사용자로부터 입력을 받아 기능 요구사항을 구현한다.
  - 저장을 위해 자체 메모리를 사용할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
- 기능 요구사항
  - 회원이 추가되고, 모든 회원이 조회될 수 있다.
    - 회원은 NORMAL과 VIP 두 가지 등급이 있다.
  - 특정 회원의 주문이 추가되고, 특정 회원의 주문들이 조회될 수 있다.
    - 회원 등급에 따라 할인 정책을 적용할 수 있다.
    - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용한다. (나중에 변경 될 수 있다.)
    - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다.



이번 요구사항은 하루 만에 내가 원하는 결과물이 나올 것 같지 않다.

따라서 여러 시리즈에 걸쳐서 아쉬운 부분들이 개선되는 여러 버전이 나올 것이다.



<br/>



## 목차


- [기본 구조](#기본-구조)
- [DIP원칙을 지키지 못한 회원주문](#dip원칙을-지키지-못한-회원주문)
  - [3Layered Architecture로 관심사 분리하기](#3layered-architecture로-관심사-분리하기)
  - [DTO로 계층 간 데이터 전달하기](#dto로-계층-간-데이터-전달하기)
  - [구현 결과 및 개선할 점](#구현-결과-및-개선할-점)
- [마치며](#마치며)


<br/>





## 기본 구조

들어가기 앞서 기본 구조를 간단하게 적어두려 한다.


<div align="center"><img width="200" alt="스크린샷 2024-11-15 오후 3 08 39" src="https://github.com/user-attachments/assets/54778cc8-31cf-472c-8e28-1c4c7675591e">
</div>

위의 패키지 구조를 보면, 크게 도메인별로 member패키지와 order 패키지로 구성했고,

비즈니스 도메인과 관련이 없는 구조를 위해 필요한 애너테이션이나 내가 만든 편의 기능들이 있는 config패키지로 구성했다.

<br/>

MemberOrderApp을 실행시키면 config에 있는 ComponentScanner를 실행시켜, member 패키지와 order패키지로 부터 값을 불러 온다.

```java
@Controller
@CommandMapping("/members")
public class MemberController {
  private final MemberService memberService = new MemberServiceImpl();

  @CommandMapping(method="POST", value="")
  public String create() {}
  
  @CommandMapping(method="GET", value="")
  public String findAll() {}
}

@Controller
@CommandMapping("/orders")
public class OrderController {
  private final OrderService orderService = new OrderServiceImpl();

  @CommandMapping(method = "POST")
  public String create() {}

  @CommandMapping(method="GET", value="")
  public String findAllByMember() {}
}
```

위와 같이 Controller를 구성했고, 이대로 MemberOrderApp을 실행시키면

<div align="center"><img width="350" alt="스크린샷 2024-11-15 오후 3 18 04" src="https://github.com/user-attachments/assets/9eab8ca8-1ca0-4d7f-990f-86fe5c92f241">
</div>

위와 같이 콘솔에서 입력을 받을 수 있다.



<br/>



어떻게 별도의 라이브러리 설치 없이 이런 구성이 가능한가 궁금하면! 어제 작성한 [[리플렉션으로 OCP 컨트롤러 만들기]](../241114_리플렉션)을 보도록 하자

<br/>

## DIP원칙을 지키지 못한 회원주문

우선 나는 요구사항에 대해 다음과 같은 다이어그램으로 구성하려 했다.

<div align="center"><img width="700" alt="스크린샷 2024-11-15 오후 3 35 50" src="https://github.com/user-attachments/assets/ba08b29b-99f2-41a2-b669-80a9c9f693b2"></div>

3Layered 아키텍처를 적용하고, 주문을 할 때 사용자 정보가 필요하기 때문에 OrderService에서 MemberRepository에 의존하도록 할 것이다.

각 Layer에는 알맞은 DTO를 통해 데이터를 주고 받을 것이다.



왜 이런 구성을 했는지 살펴보자

<br/>

### 3Layered Architecture로 관심사 분리하기

3Layered Architecture를 사용하는 이유는 객체지향 5원칙 중 첫번째인 **단일책임의 원칙** 때문이라고도 할 수 있고, 혹은 **관심사의 분리**라고도 할 수 있겠다.

Controller(Presentation Layer)는 사용자의 데이터를 우리가 만든 비즈니스 로직으로 전달하고 응답하는데 주 관심사를 둔다.

Service(Business Layer)는 비즈니스 로직을 수행하는데 주 관심을 두고,

Repository(Persistence Layer)는 영속성을 구현하기 위해, 데이터를 가져오고 저장하는 것에 주 관심사를 둔다.

```java
public class MemberController {
  private final MemberService memberService;
}
```

```java
public class MemberService {
  private final MemberRepository memberRepository; 
}
```

```java
public class MemberRepository {}
```

위와같이 Controller는 Service에 의존하고 있지만, 내부 로직이 어떻게 수행되는지 알 필요가 없고,

Service는 Repository에 의존하고 있지만 마찬가지로 데이터를 어디서 어떻게 가져오는 지에 대한 방법은 모른다.

<br/>

### DTO로 계층 간 데이터 전달하기

DTO(Data Transfer Object)는 계층 간 데이터 전송을 위해 도메인 모델 대신 사용되는 객체이다.

DTO는 순수하게 데이터를 저장하고, 데이터에 대한 getter, setter 만을 가져야한다고 하며 즉, **어떠한 비즈니스 로직을 가져서는 안되고**, 저장, 검색, 직렬화, 역직렬화 로직만을 가져야 한다고 한다.

예를 들면 사용자 정보를 조회할때 컨트롤러 쪽에서 사용자 비밀번호까지 같이 조회되도록 하는 걸 정제된  필드들만 제공한다거나, 도메인 모델에 있는 메서드를 특정 계층에서 호출하지 못하도록 하는 효과를 볼 수 있다.


<br/>


### 구현 결과 및 개선할 점

구현하여 MemberOrderApp을 실행시킨 결과이다.

<div align="center"><img width="450" alt="스크린샷 2024-11-15 오후 4 11 36" src="https://github.com/user-attachments/assets/a946e657-351b-4f37-b754-9b260a8f6344"></div>

아직 아쉬운 점이 있다.

Controller가 Service 인터페이스가 아닌 구현체에 의존하고 있다는 점, Service가 Repository인터페이스가 아닌 구현체에 의존하고 있다는 점이다.

초기 계획한 설계와는 달리 현재 내가 개발한 코드는 아래와 같은 구조를 띄고 있다.



<div align="center"><img width="750" alt="스크린샷 2024-11-15 오후 4 13 43" src="https://github.com/user-attachments/assets/5dd117bc-d41c-4073-8c98-aa312532007d"></div>



어떻게 보면 단일 책임의 원칙을 벗어났다고 볼 수 있다.

Service계층 입장에서 비즈니스 로직 구현 이외에, 어떤 구현체를 사용할지 선택까지 한다는 책임이 있는 것이다.

이렇게 되면 MemoryMemberRepository가 JdbcMemberRepository로 변경 될때 Service 파일이 빨간 색이 뜰 것이다. 확장에 닫혀있는 것이다.

우선 의존성을 주입받도록 변경하고, 이 의존관계들을 쉽게 관리해주는 Config를 추가해야 겠다는 생각이 든다.

<br/>



## 마치며

후다닥 할 수 있을 줄 알았는데 생각보다 시간이 많이 걸려서 힘들었다.

하지만, 저 Controller에서 @CommandMapping 해서 api 요청처럼 할 수 있다는 게 너무 기쁘다.

로컬 서버를 켜거나 하지 않아도, 빠르게 내가 만든 비즈니스 로직을 확인해 볼 수 있게끔

생산성이 높아지는 환경을 만든 것 같다.

다음에는 아마 애너테이션으로 `@Config` 애너테이션이 있는 곳의 `@Bean` 들을 읽어 의존관계 설정해주는 기능을 추가하지 않을까 싶다.
