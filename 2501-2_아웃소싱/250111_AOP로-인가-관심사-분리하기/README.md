# AOP로 인가 관심사 분리하기

현재 TempController라는 컨트롤러 아래에 2개의 핸들러가 있다.

```java
@RestController
public class TempController {
  @PostMapping("/user-only")
  public ResponseEntity<String> userOnly(@Auth AuthUser authUser) {
    return ResponseEntity.ok("userOnly 정상 호출");
  }

  @PostMapping("/owner-only")
  public ResponseEntity<String> ownerOnly(@Auth AuthUser authUser) {
    return ResponseEntity.ok("ownerOnly 정상 호출");
  }
}
```

이때 `authUser.userRole`에 따라서 "OWNER"는 ownerOnly만, "USER"는 userOnly만 호출이 가능하도록 해보자

<br/>

### 목차

- [AOP 적용 이유](#aop-적용-이유)
- [Annotaion 추가하기](#annotaion-추가하기)
- [포인트 컷 작성하기](#포인트-컷-작성하기)
- [어드바이스 로직 작성하기](#어드바이스-로직-작성하기)
- [포인트 컷으로 args 매칭하기](#포인트-컷으로-args-매칭하기)


<br/>

### AOP 적용 이유

사실 너무 쉬운 문제다. 아래처럼 if문으로 사용자가 맞는지만 체크하면 되는 것이다.

```java
public ResponseEntity<String> userOnly(@Auth AuthUser authUser) {
  if (!UserRole.USER.equals(authUser.userRole) {
  throw new RuntimeError("USER 아님");
  }
	return ResponseEntity.ok("userOnly 정상 호출");
}
```

하지만, 이렇게 되면 모든 컨트롤러에 저 if문을 추가해줘야 한다.

그러다가 USER가 아니라 MEMBER로 바뀐다면? 

저런 공통 관심 사항(횡단 관심사)이 우리의 핵심 관심 사항과 같이 있게되면, 이 둘의 변경 주기도 다를 뿐더러, 각 관심 사항에 집중할 수가 없다.

이 둘을 분리해야 한다. 즉, AOP를 적용해야 한다.

<br/>

### Annotaion 추가하기

User와 Owner를 구분할 애너테이션을 두 개 추가하자.

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserCheck {
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnerCheck {
}
```

우리가 AOP를 추가할 때 포인트컷으로 해당 메서드를 찾을 수 있도록, 이 애너테이션들을 메서드 위에 붙여줄 것이다.

메서드에만 붙여줄 것이기 때문에 ElementType.METHOD로 설정해주었고, 최소한 리플렉션을 통해 빈이 생성되는 시점까지는 정보가 살아있어야 하므로 RUNTIME 을 설정해주었다.

```java
@UserCheck
@PostMapping("/user-only")
public ResponseEntity<String> userOnly(@Auth AuthUser authUser) {...}

@OwnerCheck
@PostMapping("/owner-only")
public ResponseEntity<String> ownerOnly(@Auth AuthUser authUser) {...}
```

위처럼 이쁘게 달아주자.

<br/>

### 포인트 컷 작성하기

잠깐 용어정리 좀 해보자.

우리가 추가할 부가기능을 **어드바이스**라고 하면, 어드바이스가 적용될 수 있는 메서드를 **조인 포인트**라 하고,

조인 포인트들 중에서 어드바이스가 적용될 위치를 선별하도록 표현식을 통해 지정한 것을 **포인트 컷**이라 한다.

그리고 어드바이스와 포인트 컷을 합쳐 **어드바이저**라 부른다.

<br/>

특정 애너테이션을 가진 조인 포인트를 매칭하기 위한 방법으로는 포인트 컷에 `@annotation`을 사용하는 것이다.

```java
@Aspect
@Component
public class AuthCheckAspect {
  @Before("@annotation(com.example.demo.annotation.OwnerCheck)")
  public void ownerCheck(JoinPoint joinPoint) {
    // Owner 확인 로직
  }

  @Before("@annotation(com.example.demo.annotation.UserCheck)")
  public void userCheck(JoinPoint joinPoint) {
 				// User 확인 로직
  }
}
```

위처럼 OwnerCheck와 UserCheck 애너테이션이 있는 경로를 함께 명시해주었다.

조인 포인트가 실제로 호출되기 이전에 호출되면 되므로 `@Before`를 달아주었다.

<br/>

### 어드바이스 로직 작성하기

이제 권한을 확인하는 부가로직을 작성해보자 우선 ownerCheck를 기준으로 적어보겠다.

```java
@Before("@annotation(com.example.demo.annotation.OwnerCheck)")
public void ownerCheck(JoinPoint joinPoint) {
	Arrays.stream(joinPoint.getArgs())
  .filter(AuthUser.class::isInstance)
  .map(AuthUser.class::cast)
  .filter(AuthUser::isOwner)
  .findFirst()
	.orElseThrow(() -> new RuntimeException("OWNER 아님"));
}
```

우선 메서드로부터  AuthUser 타입을 가져와야 해서 `filter(AuthUser.class::isInstance)`, `map(AuthUser.class::cast)` 을 사용했고, AuthUser의 isOwner메서드 호출시 true가 아니라면 에러를 반환하게 했다.

<br/>

### 포인트 컷으로 args 매칭하기

위에서 작성한 것도 충분히 깔끔하지만 뭔가 너무 길고.. 그리고 AuthUser를 타입으로 안붙이고 저 애너테이션만 붙여놓는 바보가 있으면.. 에러가 생긴다.

`args`를 사용하면 주어진 타입이 있는 조인 포인트만 매칭할 뿐더러, 인자로 불러올 수도 있다.

userCheck는 이 방법으로 구현해보자

```java
@Before("@annotation(com.example.demo.annotation.UserCheck) && args(authUser, ..)")
public void userCheck(AuthUser authUser) {
  if (!authUser.isUser()) {
    throw new RuntimeException("USER 아님");
  }
}
```

코드가 훨씬 더 간결해지고, 이 어드바이저가 정확히 어떤걸 하고자 하는지도 빠르게 파악할 수 있게 되었다.

실행결과 잘 동작한다.

<div align="center"><img width="525" src="https://github.com/user-attachments/assets/69a03bc8-f9b7-44d8-914d-65fc85cc674c" /></div>

