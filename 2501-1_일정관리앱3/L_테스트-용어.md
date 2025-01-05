# 테스트 용어

### 목차

- [테스트 3분류](#테스트-3분류)
- [SUT(System under test): 테스트 하려는 대상](#sutsystem-under-test-테스트-하려는-대상)
- [BDD(Behavior driven development)](#bddbehavior-driven-development)
- [상태 기반 검증 vs 행위 기반 검증](#상태-기반-검증-vs-행위-기반-검증)
- [테스트 픽스처(Test fixture)](#테스트-픽스처test-fixture)
- [테스트 대역 (Test Double)](#테스트-대역-test-double)

<br/>

### 테스트 3분류

- 흔히 말하는 3분류

  - E2E: api 테스트
  - Integration: 통합테스트
  - Unit: 단위 테스트

  이거 사람마다 다 다르게 생각해서 “구글엔지니어는 이렇게 일한다”에서 멋지게 풀어냄. 소형/중형/대형 테스트라는 용어로 사용한다.

- 소형 / 중형 / 대형

  - 소형(Unit)
    - 단일 서버 / 단일 프로세스 / 단일 스레드에서 돌아가는 테스트를 의미 DISK IO가 있으면 안되고, Blocking call이 있어서도 안된다. 그래서 Thread.sleep이 테스트에 있으면 소형 테스트가 아닌 것
    - 정말 중요해! 항상 “결정적”이고, 속도가 빨라진다.

  - 중형(Integration)
    - 단일 서버 / 멀티 프로세스 / 멀티 스레드 사용 가능한 테스트를 의미하며 소형 테스트보다 느리고, 멀티 스레드 환경에서 어떻게 동작할지 모르기 때문에 결과가 항상 같다는 보장을 하지 못한다. 결과가 h2같은 외부 모듈의 동작에 따라 달라지기 때문이다.
    - 중형테스트를 소형테스트보다 더 많이 만드는 실수는 좋은 방향성이 아니다.

  - 대형(E2E, api테스트)
    - 멀티서버까지 가능한 테스트를 의미한다.

<br/>

### SUT(System under test): 테스트 하려는 대상

```java
@Test
void 유저는_북마크를_toggle_추가_할_수_있다() {
	//given
	User user = User.builder()
		.bookmark(new ArrayList())
		.build();
	
	//when
	user.toggleBookmark("my-link");
	
	//then
	boolean result = user.hasBookmark("my-link");
	assertThat(result).isTrue();
}
```

위와 같이 사용자가 북마크를 했을 때 북마크 결과가 제대로 됐는지 비교하는 테스트 코드가 있다. 이 경우 user가 sut인 것이다.

<br/>

### BDD(Behavior driven development)

TDD에 하나 더 얹힌 개념으로 BDD를 깊게 파면 DDD얘기도 나오고 애자일 얘기도 나오는데.. 복잡한 이야기를 다루지만 요약해보자.

테스트를 작성하다 보면 어느 순간 '어디에 어떻게 테스트를 넣어야 하지?'라는 질문을 마주하게 된다. 그때 BDD는 "행동에 집중해라"라고 말한다.

따라서 user가 시스템을 사용하는 user story를 강조하고 시나리오를 강조한다. 그리고 이를 지키기 위한 뼈대로 테스트 코드 작성시 아래와 같은 given, when, then을 사용하라고 권유한다.

```java
@Test
void test() {
	//given
	어떤 상황이 주어졌을 때
	//when
	이 행동을 하면
	//then
	결과가 이렇더라
}
```

어휘만 다를 뿐 3A 방식이라고도 한다. Arrange - Act - Assert

<br/>

### 상태 기반 검증 vs 행위 기반 검증

나오는 결괏값을 기댓값과 비교하는 테스트 방식을 상태 기반 검증이라 하며

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/1aa53adb-fb20-4354-aa50-da0bf7022467" /></div>

메서드가 실제로 호출이 됐는지를 검증하는 테스트를 행위 기반 검증(= 상호 작용 테스트)라 한다.

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/fb9df24c-a141-4a56-b458-cc46d586dff7" /></div>

근데 일반적으로, 이렇게 메서드가 실제로 호출이 됐는지 검증하는 방법은 그닥 좋은 방법은 아니다. 이게 내부 구현을 어떻게 했는지 **감시하는 것**이기 때문이다. (캡슐화에 위배 되는 것이다.)

우리는 그냥 객체에게 위임한 책임을, 이 객체가 제대로 수행했는지만 확인하면 되는데 일을 이리해라 저리해라 감시하는 것이다. 그러면 구현에 집착하게 된다.

물론 이 부분은 개개인마다 호불호가 있는 내용이다.

<br/>

### 테스트 픽스처(Test fixture)

- 테스트를 위해 필요한 자원이 있다면 `@BeforeEach` 를 통해 미리 생성해 두는 것을 보고 test fixture라고 부른다.
- 테스트 픽스처는 sut가 될 수도 있고, sut에 들어가야 하는 의존성 일부(협력 객체)가 될 수도 있다.
- 테스트가 한 눈에 안들어온다는 이유로 코드 중복이 진짜 심하게 발생하지 않는 한, 선호하지 않는다는 몇몇 의견들도 있다.

<br/>

### 테스트 대역 (Test Double)

<div align="center"><img width="400" src="https://github.com/user-attachments/assets/c1d989de-167e-41a1-b933-19d299845cb0" /></div>

회원가입에 이메일 발송이 된다면 회원가입 하는 코드를 테스트할 때마다 이메일이 발송되어야 할까?

이메일을 발송하는 객체 대신 가짜 객체를 넣어준다.

이 가짜 객체를 테스트 대역이라 부른다.

- Dummy:  아무런 동작도 하지 않고, 그저 코드가 정상적으로 돌아가기 위해 전달하는 객체

  <img width="800" src="https://github.com/user-attachments/assets/aeb5cd02-ace1-4dc0-89d4-07cb02972ac7" />

  이메일 보낼때 sender에 아무런 동작도 하지 않는 Dummy 객체를 넣어주고 있다. 그리고 더미 객체에 실제로 이메일을 보내는 send부분에는 아무런 구현이 없다.

- Fake: Local에서 사용하거나 테스트에서 사용하기위해 만들어진 가짜 객체, dummy와 달리 자체적인 로직이 있다.

  <img width="800" src="https://github.com/user-attachments/assets/340fad42-5ed8-4f45-966e-1afc6c1a0b59" />

  - 회원가입 메일 내용이 제대로 만들어졌는지 테스트 하고 싶은 상황
  - 잘 만들어진 Fake는 테스트할 때 말고도, 로컬 개발을 할 때도 사용가능해서 쓰임새가 다양하다.

- stub: 미리 준비된 값을 출력하는 객체

  <img width="800" src="https://github.com/user-attachments/assets/0c5038b6-292c-40d8-8c22-6f9190be8e36" />

  - 외부 연동하는 컴포넌트들에 사용을 많이 한다.
  - 객체에 어떤 일을 시켰을때 미리 준비된 값을 내려주는 것이다.
  - 보통 오른쪽 처럼 mockito프레임워크를 이용해서 구현된다.

- mock: 메소드 호출을 확인하기 위한 객체, 자가 검증 능력을 갖춤

  <img width="320" src="https://github.com/user-attachments/assets/f2e2b64f-c128-4e21-bd26-6f155ef76275" />

  test double하고도 거의 동일한 개념이 되어 버려서, stub도 dummy도 fake도 모두 mock이라 부른다.

- spy: 메소드 호출을 전부 기록했다가 나중에 확인하기 위한 객체

  <img width="313" src="https://github.com/user-attachments/assets/08c9a275-6d8d-49ba-9851-ff1598b1a7eb" />
