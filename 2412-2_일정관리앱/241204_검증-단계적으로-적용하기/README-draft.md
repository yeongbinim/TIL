# 검증 단계적으로 적용하기

회원 생성/조회 서비스에 검증 로직을 추가해볼 것이다.

요구사항은 아래와 같다.


- 타입 검증
  - [x] money 필드에 문자가 들어가면 검증 오류 처리

- 필드 검증
  - [x] name: 영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자
  - [x] money: 1000원 이상, 1백만원 이하
  - [x] password: 영어와 숫자로만, 첫 글자는 대문자, 최소 4자, 최대 14자

- 특정 필드의 범위를 넘어서는 검증
  - [x] 이름이 `admin_` 으로 시작하면 비밀번호는 `admin_` 으로 시작해야 한다.

<br/>

### 목차

- [ver1: Controller에서 검증해서 Model에 추가](#ver1-controller에서-검증해서-model에-추가)
- [ver2: Controller에서 검증해서 BindingResult에 추가](#ver2-controller에서-검증해서-bindingresult에-추가)
- [ver2.1: BindingResult](#ver21-bindingresult)


<br/>

### ver1: Controller에서 검증해서 Model에 추가

가장 먼저 생각나는 건 Controller에서 요청을 검증해서, 넘겨받는 model에 에러를 담아주는 것이다.

```java
@PostMapping("/add")
public String createMember(@ModelAttribute("member") MemberCreate memberCreate, RedirectAttributes redirectAttributes, Model model) {
  Map<String, String> errors = new HashMap<>();

  //필드 검증
  if (!StringUtils.hasText(memberCreate.getName())
    || !memberCreate.getName().matches("^[a-z][a-zA-Z0-9]{3,13}$")) {
    errors.put("name", "영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자");
  }

  중략..

  //복합 검증
  if (memberCreate.getName().startsWith("admin")
    && !memberCreate.getPassword().startsWith("admin")) {
    errors.put("globalError", "사용할 수 없는 아이디와 비밀번호 조합 입니다.");
  }

  //검증에 실패하면 다시 입력 폼으로
  if (!errors.isEmpty()) {
    model.addAttribute("errors", errors);
    return "member/new-form";
  }

  Member member = memberRepository.save(Member.from(memberCreate));
  redirectAttributes.addAttribute("id", member.getId());
  return "redirect:/members/{id}";
}
```

이렇게 했을 때 문제점은 타입 오류 처리가 안된다는 점이다. `money`처럼 숫자 타입인 곳에 문자가 들어오면 오류가 발생한다. 

그런데 이러한 오류는 스프링 MVC에서 컨트롤러에 진입하기도 전에 예외가 발생하기 때문에, 컨트롤러가 호출되지도 않고, 400 예외가 발생 하면서 오류 페이지를 띄워준다.

<br/>

### ver2: Controller에서 검증해서 BindingResult에 추가

스프링 프레임워크에서 **데이터 바인딩(웹 요청으로부터 받은 파라미터들을 자바 객체에 매핑) 과정에서** 발생할 수 있는 오류를 관리하고 접근할 수 있는 방법을 제공하는데, 바로 `BindingResult` 라는 인터페이스이다.

> - `BindingResult` 는 인터페이스이고, `Errors` 인터페이스를 상속받고 있다.
> - 실제 넘어오는 구현체는 `BeanPropertyBindingResult` 라는 것인데, 둘다 구현하고 있으므로 `BindingResult` 대신에 `Errors` 를 사용해도 된다. 
> - `Errors` 인터페이스는 단순한 오류 저장과 조회 기능을 제공한다. `BindingResult` 는 여기에 더해서 추가적인 기능들을 제공한다. 
> - `addError()` 도 `BindingResult` 가 제공하므로 여기서는 `BindingResult` 를 사용하자. 주로 관례상 `BindingResult` 를 많이 사용한다.

이것을 적용하여, 타입에러를 잡고, 에러를 추가해 보자.

```java
@PostMapping("/add")
public String createMember(@ModelAttribute("member") MemberCreate memberCreate, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
  //필드 검증
  if (!StringUtils.hasText(memberCreate.getName())
    || !memberCreate.getName().matches("^[a-z][a-zA-Z0-9]{3,13}$")) {
    bindingResult.addError(new FieldError("member", "name", memberCreate.getName(), false, null, null, "영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자"));
  }
  ... 중략
  
  //복합 검증
  if (memberCreate.getName().startsWith("admin")
    && !memberCreate.getPassword().startsWith("admin")) {
    bindingResult.addError(new ObjectError("member", null, null, "사용할 수 없는 아이디와 비밀번호 조합 입니다."));
  }

  if (bindingResult.hasErrors()) {
    return "member/new-form";
  }

  Member member = memberRepository.save(Member.from(memberCreate));
  redirectAttributes.addAttribute("id", member.getId());
  return "redirect:/members/{id}";
}
```

- `BindingResult bindingResult` 파라미터의 위치는 `@ModelAttribute modelAttribute` 다음에 와야 한다.
- `bindingReulst.addError` 를 통해 필드에러 또는 글로벌 에러를 추가할 수 있다.
- **FieldError 생성자 요약**
  
  ```java
  public FieldError(String objectName, String field, String defaultMessage) {}
  ```
  필드에 오류가 있으면 `FieldError` 객체를 생성해서 `bindingResult` 에 담아두면 된다.
  - `objectName` : `@ModelAttribute` 이름
  - `field` : 오류가 발생한 필드 이름
  - `defaultMessage` : 오류 기본 메시지
- **ObjectError 생성자 요약**
  ```java
  public ObjectError(String objectName, String defaultMessage) {}
  ```
  특정 필드를 넘어서는 오류가 있으면 `ObjectError` 객체를 생성해서 `bindingResult` 에 담아두면 된다. 
  - `objectName` : `@ModelAttribute` 의 이름
  - `defaultMessage` : 오류 기본 메시지

- 오류가 발생하는 경우 고객이 입력한 내용이 모두 사라지길 방지하기 위해 알아야 할 오버로딩된 생성자

  ```java
  public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure, @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage)
  ```

  - `rejectedValue` : 사용자가 입력한 값(거절된 값)
  - `bindingFailure` : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값
  - `codes` : 메시지 코드
  - `arguments` : 메시지에서 사용하는 인자

  여기서 `rejectedValue` 가 바로 오류 발생시 사용자 입력 값을 저장하는 필드다.

  `bindingFailure` 는 타입 오류 같은 바인딩이 실패했는지 여부를 적어주면 된다. 여기서는 바인딩이 실패한 것은 아
  니기 때문에 `false` 를 사용한다.

<br/>

### ver2.1: BindingResult

그런데, `FieldError` , `ObjectError` 는 다루기 너무 번거롭다. 오류 코드도 좀 더 자동화 할 수 있지 않을까?

컨트롤러에서 `BindingResult` 는 검증해야 할 객체인 `target` 바로 다음에 온다. 따라서 `BindingResult` 는 이미 본인이 검증해야 할 객체인 `target` 을 알고 있다.

```java
log.info("objectName={}", bindingResult.getObjectName());
log.info("target={}", bindingResult.getTarget());

// objectName=item
// target=Item(id=null, itemName=상품, price=100, quantity=1234)
```

`BindingResult` 가 제공하는 `rejectValue()` , `reject()` 를 사용하면 `FieldError` , `ObjectError` 를 직접 생성하지 않고, 깔끔하게 검증 오류를 다룰 수 있다.

```java
bindingResult.rejectValue("filed", "errorCode", errorArgs[], "defaultMessage");
bindingResult.reject("errorCode", errorArgs[], "defaultMessage");
```
- field: 오류가 발생한 필드의 이름입니다. 이 필드 이름은 폼 백엔드에서 사용하는 모델의 속성 이름과 일치해야 합니다. 예를 들어, name, password, email 등이 될 수 있습니다.

- errorCode: 오류 코드로, 메시지 소스 파일에서 이 오류 코드에 해당하는 메시지를 찾아 사용자에게 보여줄 수 있습니다. 이 코드는 국제화(i18n) 지원 시 사용될 수 있으며, 개발자가 직접 정의할 수 있습니다.

- errorArgs: 오류 메시지 형식을 지정할 때 사용할 인자들의 배열입니다. 예를 들어, 오류 메시지가 특정 숫자나 조건을 포함해야 할 경우 이 인자들을 사용할 수 있습니다.

- defaultMessage: 오류 코드에 해당하는 메시지를 메시지 소스에서 찾을 수 없을 때 사용할 기본 메시지입니다. 이 메시지는 사용자에게 직접 보여지며, 코드의 명확성을 높이기 위해 가능한 상세하게 작성하는 것이 좋습니다.

이것을 



4
컨트롤러에서 검증 로직이 차지하는 부분은 매우 크다. 이런 경우 별도의 클래스로 역할을 분리하는 것이 좋다. 그리고
이렇게 분리한 검증 로직을 재사용 할 수도 있다.


스프링이 `Validator` 인터페이스를 별도로 제공하는 이유는 체계적으로 검증 기능을 도입하기 위해서다. 그런데 앞 에서는 검증기를 직접 불러서 사용했고, 이렇게 사용해도 된다. 그런데 `Validator` 인터페이스를 사용해서 검증기를 만들면 스프링의 추가적인 도움을 받을 수 있다.

---
5
검증 로직을 매번 이렇게 작성하는건 역시나 번거롭다. 특정 필드에 대한 검증은 매우 일반적이다. 이것도 애너테이션으로 해보자는 의견이 나왔다.

먼저 Bean Validation은 특정한 구현체가 아니라 Bean Validation 2.0(JSR-380)이라는 기술 표준이다. 쉽게 이야기해서 검증 애노테이션과 여러 인터페이스의 모음이다. 마치 JPA가 표준 기술이고 그 구현체로 하이버네이트가 있는 것과 같다.

Bean Validation을 구현한 기술중에 일반적으로 사용하는 구현체는 하이버네이트 Validator이다. 이름이 하이버네이트가 붙어서 그렇지 ORM과는 관련이 없다.


Bean Validation을 달면 이 애너테이션을 읽어서 작동할 수 있는 Validator가 있어야 겠지?

`spring-boot-starter-validation` 의존관계를 추가하면 두개의 라이브러리가 추가된다.
`jakarta.validation-api` : Bean Validation 인터페이스, `hibernate-validator` 구현체

`@Validated` 는 스프링 전용 검증 애노테이션이고, `@Valid` 는 자바 표준 검증 애노테이션이다. 둘중 아무거나
사용해도 동일하게 작동하지만, `@Validated` 는 내부에 `groups` 라는 기능을 포함하고 있다. 


검증 순서

1. `@ModelAttribute` 각각의 필드에 타입 변환 시도 
   1. 성공하면 다음으로
   2. 실패하면 `typeMismatch` 로 `FieldError` 추가
2. Validator 적용


**바인딩에 성공한 필드만 Bean Validation 적용**
BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다.
생각해보면 타입 변환에 성공해서 바인딩에 성공한 필드여야 BeanValidation 적용이 의미 있다. (일단 모델 객체에 바인딩 받는 값이 정상으로 들어와야 검증도 의미가 있다.)


`return bindingResult.getAllErrors();` 는 `ObjectError` 와 `FieldError` 를 반환한다. 스프링이 이 객 체를 JSON으로 변환해서 클라이언트에 전달했다. 여기서는 예시로 보여주기 위해서 검증 오류 객체들을 그대로 반환 했다. 실제 개발할 때는 이 객체들을 그대로 사용하지 말고, 필요한 데이터만 뽑아서 별도의 API 스펙을 정의하고 그에 맞는 객체를 만들어서 반환해야 한다.



-- 타입 안맞으면 에러남 --
HTTP 요청 파리미터를 처리하는 `@ModelAttribute` 는 각각의 필드 단위로 세밀하게 적용된다. 그래서 특정 필드 에 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리할 수 있었다.
`HttpMessageConverter` 는 `@ModelAttribute` 와 다르게 각각의 필드 단위로 적용되는 것이 아니라, 전체 객체 단위로 적용된다.
따라서 메시지 컨버터의 작동이 성공해서 `ItemSaveForm` 객체를 만들어야 `@Valid` , `@Validated` 가 적용된다.



**참고**
`HttpMessageConverter` 단계에서 실패하면 예외가 발생한다. 예외 발생시 원하는 모양으로 예외를 처리하는 방법
은 예외 처리 부분에서 다룬다.