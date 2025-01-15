# 로그 추적기

우선 기본 틀은 아래와 같다.

```java
@RestController
@RequiredArgsConstructor
public class MemberControllerV0 {
  private final MemberServiceV0 memberService;

  @GetMapping("/v0/request")
  public String request(String memberId) {
    memberService.createMember(memberId);
    return "ok";
  }
}


@Service
@RequiredArgsConstructor
public class MemberServiceV0 {
  private final MemberRepositoryV0 memberRepository;

  public void createMember(String memberId) {
    memberRepository.save(memberId);
  }
}

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV0 {
  public void save(String memberId) {
    // 저장 로직
    if (memberId.equals("ex")) {
      throw new IllegalArgumentException("예외 발생!");
    }
    sleep(1000);
  }
}
```

위와같은 Controller, Service, Repository가 있고, 각 메서드가 호출되는 시점과 메서드가 종료되는 시점, 에러가 발생하는 시점의 시간차를 로깅하고 싶다.

그리고, 메서드 호출되는 깊이가 깊어질수록 그 계층 관계가 표현되었으면 한다.

<br/>

### 목차

- [v1: 프로토타입 개발](#v1-프로토타입-개발)
- [v2: 파라미터로 동기화 개발](#v2-파라미터로-동기화-개발)
- [v3-1: 필드 동기화](#v3-1-필드-동기화)
- [v3-2: 쓰레드 로컬 동기화](#v3-2-쓰레드-로컬-동기화)


<br/>

### v1: 프로토타입 개발

우선 [TraceId](./src/trace/TraceId.java), [TraceStatus](./src/trace/TraceStatus.java) 클래스를 만들고, 본격적인 로그 추적기를 아래처럼 작성한다.

```java
@Slf4j
@Component
public class HelloTraceV1 {
  // 중략
  public TraceStatus begin(String message) {
    TraceId traceId = new TraceId();
    Long startTimeMs = System.currentTimeMillis();

    log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);

    return new TraceStatus(traceId, startTimeMs, message);
  }

  public void end(TraceStatus status) {
    Long stopTimeMs = System.currentTimeMillis();
    Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
    TraceId traceId = status.getTraceId();

    log.info("[{}] {}{} time={}ms", traceId.getId(),
      addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);
  }

  public void exception(TraceStatus status, Exception e) {
    Long stopTimeMs = System.currentTimeMillis();
    Long resultTimeMs = stopTimeMs - status.getStartTimeMs();
    TraceId traceId = status.getTraceId();

    log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
      addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());
  }
}
```

이제 각 메서드에서는 시작될때는 begin, 끝날때에는 end, 예외가 발생했을 때에는 exception을 호출하기만 하면 된다.

Service계층의 코드를 보면 아래와 같다.

```java
@Service
@RequiredArgsConstructor
public class MemberServiceV1 {
  private final MemberRepositoryV1 memberRepository;
  private final HelloTraceV1 trace;

  public void createMember(String memberId) {
    TraceStatus status = trace.begin("MemberService.createMember()");
    try {
      memberRepository.save(memberId);
      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}

```

그리고, 지금까지의 결과를 테스트해보면 다음과 같은 로그가 남는 것을 확인할 수 있다.

<div align="center"><img width="400" alt="2025-01-02_v1-suc" src="https://github.com/user-attachments/assets/e5ff7f3d-7dd3-4cda-9d2e-bb38afd4b1c1" /></div>
<div align="center"><img width="700" alt="2025-01-02_v1-ex" src="https://github.com/user-attachments/assets/26383ae7-b4dc-4544-a73e-605e099e01de" /></div>

깊이가 표현되지 않고 있다.

깊이가 표현되려면 어떻게 해야할까?

이전 TraceId 정보를 알게된다면 이 깊이를 표현할 수 있을 것 같다.

우선은 파라미터를 넘기는 방식으로 이것을 구현해보자

<br/>

### v2: 파라미터로 동기화 개발

기존의 HelloTrace에서 아래의 코드를 추가해본다.

```java
public TraceStatus beginSync(TraceId beforeTraceId, String message) {
  TraceId nextId = beforeTraceId.createNextId();
  Long startTimeMs = System.currentTimeMillis();

  log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX, nextId.getLevel()), message);

  return new TraceStatus(nextId, startTimeMs, message);
}
```

그리고 첫 메서드 호출 시점인 Controller는 그냥 begin을 사용하도록, 그리고 아래 계층에서는 Controller에서 사용한 traceId를 넘겨받도록 구현한다.

```java
@Service
@RequiredArgsConstructor
public class MemberServiceV2 {
  private final MemberRepositoryV2 memberRepository;
  private final HelloTraceV2 trace;

  public void createMember(TraceId traceId, String memberId) {
    TraceStatus status = trace.beginSync(traceId, "MemberService.createMember()");
    try {
      memberRepository.save(status.getTraceId(), memberId);
      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}

```

서비스 코드만 살펴보면, 이전과 다른 점은 createMember 인자로 traceId를 추가로 넘겨받아 beginSync 메서드를 호출하는 것이다.

<div align="center"><img width="400" alt="2025-01-02_v2-suc" src="https://github.com/user-attachments/assets/63946220-b4b7-40f2-be19-417aa6222953" /></div>
<div align="center"><img width="700" alt="2025-01-02_v2-ex" src="https://github.com/user-attachments/assets/6cb18e1d-9b21-444a-b106-d678d7906594" /></div>

실행 결과를 확인해보면 의도한대로 잘 수행되는 것을 확인할 수 있다.

하지만, 로그를 찍는 이 부가관심사 때문에 저 매개변수를 하나 더 받는건 아무래도 손해다.

이걸 개선해보자

<br/>

### v3-1: 필드 동기화

LogTrace를 이번에는 인터페이스로 만들어보겠다.

```java
public interface LogTrace {
  TraceStatus begin(String message);

  void end(TraceStatus status);

  void exception(TraceStatus status, Exception e);
}
```

그리고, 이 인터페이스를 구현하는 FieldLogTrace 구현체를 만든다.

```java
@Slf4j
public class FieldLogTrace implements LogTrace {
  private TraceId traceIdHolder;

  @Override
  public TraceStatus begin(String message) {
    syncTraceId();
    // 기존과 동일
  }

  @Override
  public void end(TraceStatus status) {
    //기존과 동일
    releaseTraceId();
  }

  @Override
  public void exception(TraceStatus status, Exception e) {
    //기존과 동일
    releaseTraceId();
  }

  private void syncTraceId() {
    if (traceIdHolder == null) {
      traceIdHolder = new TraceId();
      return;
    }
    traceIdHolder = traceIdHolder.createNextId();
  }

  private void releaseTraceId() {
    if (traceIdHolder.isFirstLevel()) {
      traceIdHolder = null;
    } else {
      traceIdHolder = traceIdHolder.createPreviousId();
    }
  }
}

```

기존과 달라진 점은 저 `traceIdHolder`이다. 이 객체가 이전 traceId에 대해서 관리하도록 변경한 것이다.

그리고 `syncTraceId`를 통해서 begin함수 시작할때 이전 traceId보다 깊이가 1 증가한 traceId를 할당하고, end나 exception 함수에서는 끝날때 `releaseTraceId`를 통해 이전 traceId를 다시 할당하도록 했다.

```java
@Service
@RequiredArgsConstructor
public class MemberServiceV3 {
  private final MemberRepositoryV3 memberRepository;
  private final LogTrace trace;

  public void createMember(String memberId) {
    TraceStatus status = trace.begin("MemberService.createMember()");
    //이전과 동일
  }
}

```

이제 v1때와 똑같이 traceId를 파라미터로 주고받지 않아도 된다.

하지만, 요청을 연속으로 두번 보내보자

<div align="center"><img width="400" alt="2025-01-02_v3-실제결과" src="https://github.com/user-attachments/assets/5bcac433-881c-4b7e-b3a9-d3a1153a7425" /></div>

뭔가 이상하다. 다른 요청인데  id가 똑같고, 깊이는 왜 저렇게 깊어지는 거지?

예상결과는 아래처럼 되는게 맞는데..

<div align="center"><img width="400" alt="2025-01-02_v3-예상결과" src="https://github.com/user-attachments/assets/6d0709f7-8060-4270-b076-d8e5fe4eb2e3" /></div>

바로 동시성 문제가 발생한 것이다.

`FieldLogTrace`는 싱글톤으로 등록된 싱글톤 빈이기 때문에, `traceIdHolder`필드를 여러 쓰레드가 동시에 접근하면서 문제가 발생한 것이다.

이 동시성 문제를 해결하려면 어떻게 해야할까? 이럴때 사용하는 것이 바로 쓰레드 로컬이다.

<br/>

### v3-2: 쓰레드 로컬 동기화

ThreadLocal은 해당 쓰레드만 접근할 수 있는 특별한 저장소를 말한다.

<div align="center"><img width="600" alt="스크린샷 2025-01-15 오전 10 20 05" src="https://github.com/user-attachments/assets/bb581015-a95c-4c56-9fd7-a6c3cc0a02ec" /></div>

threadA가 조회하면 쓰레드 로컬은 threadA 전용 보관서에서 userA 데이터를 반환해준다.

자바는 언어차원에서 쓰레드 로컬을 지원하기 위한 `java.lang.ThreadLocal` 클래스를 제공한다.

`FieldLogTrace`에서 발생했던 동시성 문제를 `ThreadLocal`로 해결해보자.

```java
@Slf4j
public class ThreadLocalLogTrace implements LogTrace {
  private final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

  // (생략) 이전과 동일 

  private void syncTraceId() {
    TraceId traceId = traceIdHolder.get();
    if (traceId == null) {
      traceIdHolder.set(new TraceId());
      return;
    }
    traceIdHolder.set(traceId.createNextId());
  }

  private void releaseTraceId() {
    TraceId traceId = traceIdHolder.get();
    if (traceId.isFirstLevel()) {
      traceIdHolder.remove();
    } else {
      traceIdHolder.set(traceId.createPreviousId());
    }
  }
}

```

`TraceId traceIdHolder` 필드르 쓰레드 로컬을 사용하도록 `ThreadLocal<TraceId> traceIdHolder`로 변경했다.

그리고, `syncTraceId`에서 `.get()`을 통해 값을 가져오고 `.set()`을 통해 값을 할당한다.

`releaseTraceId`에서는 `.remove()`를 통해 쓰레드 로컬에 저장된 값을 제거해준다.

쓰레드 로컬을 모두 사용하고 나면 꼭 `ThreadLocal.remove()`를 호출해서 쓰레드 로컬에 저장된 값을 제거해주어야 한다는 사실을 절대 잊지 말자!

<div align="center"><img width="600" alt="스크린샷 2025-01-15 오전 10 25 43" src="https://github.com/user-attachments/assets/623612c0-c0a2-4f63-816e-114c6e548c0b" /></div>

WAS처럼 스레드풀을 사용하는 경우에 위와같은 문제가 발생할 수 있기 때문이다.

<br/>

여튼 변경된 코드를 통해 다시 실행해보면

<div align="center"><img width="400" alt="2025-01-02_v3-예상결과" src="https://github.com/user-attachments/assets/6d0709f7-8060-4270-b076-d8e5fe4eb2e3" /></div>

예상대로 잘 수행되는 것을 확인할 수 있다.
