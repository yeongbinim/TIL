# 예외 클래스를 여러개 만들어야 할까

예외처리에 대한 고민이 생겼다. 클라이언트 개발자 입장에서, 그리고 서버 개발자 입장에서 에러 처리를 어떻게 하는게 좋을지 고민해보았다.

<br/>

### 목차

- [ResponseStatusException 만으로](#responsestatusexception-만으로)
- [ErrorCode를 위한 CustomException 만들기](#errorcode를-위한-customexception-만들기)
- [BaseException을 상속하는 예외들 만들고 나누기](#baseexception을-상속하는-예외들-만들고-나누기)
- [마치며](#마치며)

<br/>

### ResponseStatusException 만으로

예외가 발생하면 ExceptionHandler가 잡아줘서 마치 컨트롤러마냥 원하는 응답을 만들어줄 수 있다는 것을 안다.

따라서 아래처럼 ResponseStatusException만 잘 잡도록 하고,

```java
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ExceptionResponse> handleCustomException(
		HttpServletRequest request,
		ResponseStatusException e
	) {
		log.warn("잘못된 요청이 들어왔습니다. URI: {}, 내용:  {}",
			request.getRequestURI(),
			e.getMessage());

		return ResponseEntity
			.status(e.getStatusCode())
			.body(new ExceptionResponse(e.getReason()));
	}
}
```

로직 코드에서는 이 에러를 잘 던져주기만 하면 될 것 같다.

```java
// 매력적인 로직 생략하고 예외들만 나열

new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.");

new ResponseStatusException(HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다.");

new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일이 중복됩니다.");

new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다.");
```

여기서 한 번 클라이언트 개발자 입장에서 생각해보자

이메일을 변경하려고 이메일 정보와, 확인을 위한 비밀번호를 넘겼는데 400 에러를 받았다.

클라이언트 개발자는 사용자에게 뭐가 문제인지 친절하게 표시해줘야 되는데 서버의 메시지를 그대로 내보내면 안되지 않는가?

그렇다고 문자열 비교를 할까? 아니, 서버가 보내주는 메시지가 조금이라도 달라지면 어떻게 하려고? 그리고 한글에서 영어로 에러 메시지가 바뀌는 경우까지 고려하면 머리아프다.

클라이언트 개발자가 에러를 식별할 수 있도록 에러 코드를 던져주어야 겠다.

<br/>

### ErrorCode를 위한 CustomException 만들기

예외를 발생시킬때 해당 예외의 메시지 뿐만 아니라 예외 코드와 상태 코드까지 하나로 묶기 위해서 CustomException 클래스를 만들고, ExceptionType이라는 Enum객체를 만들어 예외들을 관리하게 해보았다.

```java
@Getter
public class CustomException extends RuntimeException {

	private final HttpStatus httpStatus;
	private final String code;

	public CustomException(ExceptionType exceptionType) {
		super(exceptionType.getMessage());
		this.httpStatus = exceptionType.getHttpStatus();
		this.code = exceptionType.getCode();
	}
}

@Getter
@RequiredArgsConstructor
public enum ExceptionType {
	FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "F01", "해당 피드를 찾을 수 없습니다."),

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U01", "해당 사용자를 찾을 수 없습니다."),
	EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "U02", "이메일이 중복됩니다."),
	PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "U03", "기존 비밀번호가 일치하지 않습니다."),
	PASSWORD_SAME(HttpStatus.BAD_REQUEST, "U04", "새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

```

이것의 이점은 어떤 예외들이 프로젝트 내에서 관리되고 있는지 명확하게 보이고, 어디서 이 예외들이 쓰이는지 빠르게 찾을 수 있다.

클라이언트 개발자는 이제 예외에 대해 응답 코드로 예외들을 식별할 수 있기 때문에 각 예외 코드에 알맞게 사용자에게 예외를 전달할 수 있다.

```java 
new CustomException(USER_NOT_FOUND);

new CustomException(EMAIL_DUPLICATE);

new CustomException(PASSWORD_NOT_MATCH);

new CustomException(PASSWORD_SAME);
```

여기서 다시 서버 개발자 입장이 되어보자.

어떤 사용자가 자꾸 "U01" 에러가 난다고 한다.

값이 어떻게 찍혔는지 확인해보려고 하니, 서비스가 너무 잘 돼서 로그가 막 이리저리 꼬여있어서 어떤 요청에 대해 저 예외가 났는지 찾기가 어렵다.

처음부터 "id가 -1인 사용자를 찾을 수 없습니다." 이런 식으로 메시지가 찍혀있다면 편할텐데...

<br/>

### BaseException을 상속하는 예외들 만들고 나누기

이렇게 각 예외를 위한 예외 클래스들을 만들어두면 예외 발생시 각 예외에 대해 세세한 결과들을 만들어 낼 수 있다.

```java
@Getter
public class DuplicateException extends BaseException {

  private String value;

  public DuplicateException(String value) {
    this(value, ErrorCode.DUPLICATE);
    this.value = value;
  }

  public DuplicateException(String value, ErrorCode errorCode) {
    super(value, errorCode);
    this.value = value;
  }
}

public class EmailDuplicateException extends DuplicateException {  
  
  public EmailDuplicateException(final String email) {  
    super(email, ErrorCode.EMAIL_DUPLICATE);  
  }  
}

public class NicknameDuplicateException extends DuplicateException {  
  
  public NicknameDuplicateException(final String nickname) {  
    super(nickname, ErrorCode.NICKNAME_DUPLICATE);  
  }  
}
```

일반적으로 서비스 시 나타날 수 있는 예외의 경우는 중복 값이 허용되지 않을 때, 없는 객체를 요청할 때, 만료된 값을 요청할 때 등이 있을 수 있는데 위의 코드는 DuplicateException 범주의 예시이다.



개인적으로 이 방법에 대해서는 조금 아쉬운게.. 많이 복잡하다는 점이다...

<div align="center"><img width="273" src="https://github.com/user-attachments/assets/467235ad-c070-4b83-9afb-39ca28e4007d" /></div>

어떤 에러를 써야할지 한눈에 보이지가 않는다. 이미 만들어져 있는 에러가 있는데 중복해서 같은 에러를 만들 수도 있을거 같고, 만들어놓고 사용되지 않는 에러도 빠르게 발견할 수 없을 것 같다..

이 에러들이 나중에는 수백개가 될텐데.. 이거 어떻게 관리하지?

<br/>

### 마치며

결국은 트레이드 오프다!!

나는 당장 몇 달 동안에는 Enum만으로 할 것이다. 그것이 개발 생산성이 더 높고, 덜 복잡하고, 팀원들이 이해하기 쉽기 때문이다.

그리고 정확히 어떤 파라미터로 인해 에러가 났는지 디버깅에 용이할 정도의 복잡한 로직은 당장의 프로젝트에는 없을 것 같다.

<br/>

아래는 관련해서 참고한 링크이다.

[Spring Custom Exception과 예외 처리 전략에 대한 고민](https://dukcode.github.io/spring/spring-custom-exception-and-exception-strategy/)

