# 로그 추적기 관심사 분리

지난 코드를 봤을때 v0대비 v3의 코드들이 너무 지저분하다.

맴버를 생성하는 핵심 기능에 부가 기능이 함께 섞여있게 되면서, 핵심 기능 코드보다 부가 기능을 처리하기 위한 코드가 더 많아졌다.

이 문제를 효율적으로 처리할 방법이 없을까?

v3를 보면 아래의 패턴이 controller, service, repository 전반적으로 나타난다.

```java
TraceStatus status = null;
try {
  status = trace.begin("message");
  //핵심 기능 호출
  trace.end(status);
} catch (Exception e) {
  trace.exception(status, e);
  throw e;
}
```

이것을 분리하고 싶다. 핵심 기능은 변하는데, 변하지 않는 부가 기능을 분리하고 싶은 것이다.


<br/>

### 목차

- [v4: 템플릿 메서드 패턴 적용](#v4-템플릿-메서드-패턴-적용)
- [v5: 전략패턴과 템플릿 콜백 적용](#v5-전략패턴과-템플릿-콜백-적용)


<br/>


### v4: 템플릿 메서드 패턴 적용

'변하는 것과 변하지 않는 것을 분리하라'라는 템플릿 메서드 패턴이 이 문제를 해결해준다.

<div align="center"><img width="300" src="https://github.com/user-attachments/assets/6827ab33-b856-4126-a409-e585f2cb763e" /></div>

```java
@RequiredArgsConstructor
public abstract class AbstractTemplate<T> {
	private final LogTrace trace;

	public T execute(String message) {
		TraceStatus status = null;
		try {
			status = trace.begin(message);

			T result = call();

			trace.end(status);
			return result;
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}

	protected abstract T call();
}
```

다음의 추상 템플릿을 만들어 두었다.

이제 저 클래스를 상속하여, call부분만 구현해주면 된다.

```java
@Service
@RequiredArgsConstructor
public class MemberServiceV4 {
	private final MemberRepositoryV4 memberRepository;
	private final LogTrace trace;

	public void createMember(String memberId) {
		AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {
			@Override
			protected Void call() {
				memberRepository.save(memberId);
				return null;
			}
		};
		template.execute("MemberService.createMember()");
	}
}

```

익명 내부 클래스를 사용해서, 객체 생성과 동시에 자식 클래스를 정의했다.

템플릿 메서드 패턴을 사용한 덕분에 핵심 기능에 좀 더 집중할 수 있게 된 것 같다.

로그를 남기는 로직이 변경된다고 가정해보자. 이 Service코드의 수정 없이, 단순히 템플릿 코드만 변경하면 된다.

단일 책임 원칙을 잘 준수한 덕분에, 이런 이점을 누릴 수 있는 것이다.

<br/>

하지만 상속을 사용하기 때문에, 자식 클래스가 부모 클래스와 강하게 의존하고 있는 단점이 있다. 자식 클래스는 부모 클래스의 기능을 전혀 사용하지 않는데, 부모 클래스를 알아야 하는 점이 문제인 것이다.

이 템플릿 메서드 패턴과 비슷한 역할을 하면서 상속의 단점을 제거할 수 있는 디자인 패턴이 전략 패턴이다.

<br/>

### v5: 전략패턴과 템플릿 콜백 적용

전략패턴은 변하지 않는 부분을 `Context`라는 곳에 두고, 변하는 부분을 `Strategy`라는 인터페이스를 만들고 해당 인터페이스를 구현하도록 해서 문제를 해결하는 방식이다.

상속이 아니라 위임으로 문제를 해결하는 것이다.

전략 패턴에서 `Context`가 변하지 않는 템플릿 역할을 하고, `Strategy`는 변하는 알고리즘 역할을 하는 것이다.

<div align="center"><img width="300" src="https://github.com/user-attachments/assets/3e8baee7-fb5d-46c3-bb46-7d88c828bbab" /></div>

코드 예제를 가볍게 살펴보면 아래와 같다.

```java
// 익명 클래스 방식
Context context1 = new Context(new Strategy() {
	@Override
	public void call() {
		log.info("비즈니스 로직1 실행");
	}
});
context1.execute();

// Strategy 인터페이스에 메서드가 하나일 경우 가능한 람다 방식
Context context2 = new Context(() -> log.info("비즈니스 로직1 실행"));
context2.execute();
```

선 조립 후 실행 방식으로, 이 방식의 단점은 조립 이후에 전략을 변경하기 어렵다는 점이다.

이렇게 먼저 조립하고 사용하는 방식보다 더 유연하게 전략 패턴을 사용하는 방법은, 전략을 필드로 갖지 않고, 실행 시점에 전략을 넘겨받아서 실행하는 방식인데, 이걸 템플릿 콜백패턴이라 한다. (Context -> Template | Strategy -> Callback)

> **콜백 정의**
>
> 프로그래밍에서 콜백(callback) 또는 콜애프터 함수(call-after function)는 다른 코드의 인수로서 넘겨주는 실행 가능한 코드를 말한다. 콜백을 넘겨받는 코드는 이 콜백을 필요에 따라 즉시 실행할 수도 있고, 아니면 나중에 실행할 수도 있다. (위키백과 참고)

이 방식을 통해서 문제를 해결해보자

```java
@RequiredArgsConstructor
public class TraceTemplate {
	private final LogTrace trace;

	public <T> T execute(String message, TraceCallback<T> callback) {
		TraceStatus status = null;
		try {
			status = trace.begin(message);

			T result = callback.call();

			trace.end(status);
			return result;
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}
}
```

우선 Template을 작성했는데, 템플릿 메서드 패턴의 AbstractTemplate때와 유사하고, 다른점은 추상 클래스가 아니라는 점과, 자식클래스의 call을 호출하는 것이 아닌, callback의 call을 호출한다는 것이다.

```java
public interface TraceCallback<T> {
  T call();
}
```

그리고 Callback 인터페이스를 위와 같이 만들고

```java
@Service
public class MemberServiceV5 {
	private final MemberRepositoryV5 memberRepository;
	private final TraceTemplate template;

	public MemberServiceV5(MemberRepositoryV5 memberRepository, LogTrace trace) {
		this.memberRepository = memberRepository;
		this.template = new TraceTemplate(trace);
	}

	public void createMember(String memberId) {
		template.execute("MemberService.createMember()", () -> {
			memberRepository.save(memberId);
			return null;
		});
	}
}
```

위와같이 기존 코드에 적용할 수 있다.

눈에띄게 좋아진 장점은, 구현해야하는 것이 이제는 추상클래스가 아니라, 인터페이스이기 때문에 람다방식으로 할 수 있는 장점이 큰 거 같다.

