# Spring Web MVC 전환하기

CGI 구현체로 동적 웹을 만드는 것부터 시작해서 서블릿을 적용하는 것, JSP를 적용하는 것 그리고 그 둘을 조합하여 MVC 패턴을 도입하는 것, 마지막으로 프론트 컨트롤러 패턴으로 디스패처 서블릿을 구현해보는 것 까지 했다.

마지막으로 스프링 MVC 에서는 어떤 애너테이션을 사용하여서 개발을 하는지, 그리고 Thymeleaf는 어떻게 적용하는지 해보기로 했다.

따라서 가볍게 아래의 요구사항을 세웠다.

- [x] 지금까지의 서비스를 Spring Web MVC를 적용하여 개선한다.
- [x] 이때 JSP가 아닌, Thymeleaf를 적용한다.

<br/>

### 목차

- [프로젝트 설정](#프로젝트-설정)
- [컨트롤러에 애너테이션 사용하기](#컨트롤러에-애너테이션-사용하기)
- [Thymeleaf 적용하기](#thymeleaf-적용하기)
- [Controller 요청 처리 구현하기](#controller-요청-처리-구현하기)
- [PRG 적용하기](#prg-적용하기)
- [마치며](#마치며)



<br/>

### 프로젝트 설정

spring boot 시작할 때 이제 JSP를 사용하지 않을 거기 때문에, 톰캣을 기본으로 내장하는 JRE 로 압축하도록 하고, spring web과 thymeleaf 를 의존성 추가한다.

혹시나 깜박했다면 아래를 build.gradle에 설정할 것이다.

```
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
```

<br/>

### 컨트롤러에 애너테이션 사용하기

핸들러 매핑 1순위인 `@RequestMapping`을 사용할 것이다.

그러기 위해서는, 빈으로 등록이 되어 있어야 되는데, 따라서 `@Component` 를 하면 될 것 같지만, 안된다.

스프링 부트 3.0(스프링 프레임워크 6.0)부터는 클래스 레벨에 `@RequestMapping` 이 있는 빈을 스프링 컨트롤러로 인식하지 않는다. 오직 `@Controller` 가 있어야 스프링 컨트롤러로 인식한다.

따라서, `@Controller` 를 적어주고, `@RequestMapping` 을 클래스에 적어줘서 경로를 이어붙이도록 할 것이다.

```java
@Controller
@RequestMapping("/members")
public class MemberController {
  private final MemberRepository memberRepository;

  @Autowired
  public MemberController(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }
}
```

MemberRepository를 Autowired 되도록 했으니 이것또한 빈으로 등록시켜줘야 하는데, @Repository 애너테이션을 사용할 것이다.

```java
@Repository
public class MemberRepository { 중략 }
```

<br/>

### Thymeleaf 적용하기

타임리프는 스프링 기반 SSR 방식의 서비스에서 최근 가장 많이 사용되는 템플릿 엔진으로,

다른 템플릿 엔진과 차별점은 순수 HTML을 그대로 유지하면서 뷰 템플릿도 사용할 수 있다는 것이다. (이것을 네츄럴 템플릿이라 한다고 한다)

JSP는 뒤죽박죽이지만 이건 태그 안에 템플릿 문법을 적용하기 때문에 정말이지 깔끔하자. 아래의 `members.html` 파일을 보자.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
</head>
<body>
<h1>Member List</h1>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>password</th>
    </thead>
    <tbody>
    <tr th:each="member : ${members}">
        <td th:text="${member.id}">회원id</td>
        <td th:text="${member.name}">이름</td>
        <td th:text="${member.password}">비밀번호</td>
    </tr>
    </tbody>
    <button onclick="location.href='new-form.html'"
            th:onclick="|location.href='@{/members/add}'|" type="button">
        회원 등록
    </button>
</table>
</body>
</html>
```

- `<html xmlns:th="http://www.thymeleaf.org">`: 타임리프 사용 선언

- ` <tr th:each="member : ${members}">`: 반복

- `th:onclick="|location.href='@{/members/add}'|"`: 리터럴 대체

  - `th:onclick="'location.href=' + '\'' + @{/members/add} + '\''"`

    이렇게 작성해야 할게 리터럴 대체를 통해 편하게 작성할 수 있다.

- 결국 thymeleaf의 핵심은 `th:xxx` 가 붙은 부분은 서버사이드에서 렌더링 되고, 기존 것을 대체한다는 것이다. `th:xxx` 이 없으면 기존 html 의 `xxx` 속성이 그대로 사용된다.

이 파일은 어디에 두어야 할까? 기존 JSP 파일은 webapp/WEB-INF에 두었었다.

이젠 JAR를 사용하기 때문에 `webapp` 경로를 사용하지 않는다.

템플릿 엔진 리졸버는 템플릿 파일을 처리하기 위한 기본 경로로 `src/main/resources/templates`를 사용한다.

타임리프도 마찬가지이기 때문에, 이 templates폴더를 만들어서 두자.

<br/>

### Controller 요청 처리 구현하기

이제 앞서 작성했던 Controller를 마저 작성해서 우리의 타임리프문법을 적용한 html이 그려지도록 해보자.

```java
@Controller
@RequestMapping("/members")
public class MemberController {
  // 중략
  @GetMapping
  public String members(Model model) {
    model.addAttribute("members", memberRepository.findAll());
    return "member/members";
  }

  @GetMapping("/add")
  public String memberForm() {
    return "member/new-form";
  }

  @PostMapping("/add")
  public String createMember(@ModelAttribute("member") MemberCreate memberCreate, Model model) {
    Member member = memberRepository.save(Member.from(memberCreate));
    model.addAttribute("member", member);
    return "member/save-result";
  }

  @PostConstruct
  public void init() {
    memberRepository.save(new Member(null, "yeim", "1234"));
    memberRepository.save(new Member(null, "hgo", "1234"));
  }
}
```

- `Model model` : ModelAndView를 반환하는 대신 String을 반환할때 이 Model파라미트럴 통해 값을 뷰로 넘길 수 있다.
- `GetMapping`: `@RequestMapping(method=GET)` 과 동일하다.
- `@PostConstruct` : 해당 빈의 의존관계가 모두 주입되고 나면 초기화 용도로 호출된다. 테스트용 데이터를 넣기 위해 적었다.
- `@ModelAttribute`: `@RequestParam` 으로 하나하나 변수를 받는 대신 한번에 저 DTO를 통해 받는게 가능하다. model에 기본적으로 member가 세팅이 되는 기능도 있다. (사실 저 애너테이션을 아예 생략해도 된다.)

<br/>

### PRG 적용하기

아직 문제가 존재한다. createMember 메서드를 보면, 멤버를 생성한 이후 "member/save-result"가 반환이 되도록 했는데, 현재 페이지에서 새로고침을 한다면 다시 이 Post 요청이 호출이 된다.

<div align="center"><img width="400" src="https://github.com/user-attachments/assets/86c49c93-ea25-44e9-954b-312ec3b057d3"/></div>

따라서 PRG(Post, Redirect, Get) 방식을 사용해서 결과에 대해서는 상세 조회 리다이렉션이 되도록 할 것이다.

```java
@Controller
@RequestMapping("/members")
public class MemberController {
  // 중략
  @GetMapping("/{memberId}")
  public String member(@PathVariable Long memberId, Model model) {
    Member member = memberRepository.findById(memberId).orElse(null);
    model.addAttribute("member", member);
    return "member/save-result";
  }

  @PostMapping("/add")
  public String createMember(@ModelAttribute("member") MemberCreate memberCreate, Model model) {
    Member member = memberRepository.save(Member.from(memberCreate));
    return "redirect:/members/" + member.getId();
  }
}
```

하지만 아직 아쉬운 점은 member.getId()를 더하는 방식은 URL 인코딩이 되지 않기 때문에, 예상치 못한 결과가 나올 수도 있다.

이를 위해 RedirectAttributes 라는 것을 스프링 MVC에서 제공해준다.

```java
@PostMapping("/add")
public String createMember(@ModelAttribute("member") MemberCreate memberCreate, RedirectAttributes RedirectAttributes) {
  Member member = memberRepository.save(Member.from(memberCreate));
  redirectAttributes.addAttribute("id", member.getId())
  return "redirect:/members/{id}";
}
```

다음과 같이 작성하며, 추가로 addAttribute 되는 것들은 쿼리 파라미터로 넘어간다.

<br/>

### 마치며

으... 직접 하나하나 만들면서 하다가 이렇게 다 제공해주니깐 너무 편하다.

옛날에는 "왜 이렇게 제공해주는 게 많아... 언제 다 외워.." 이런 느낌이었다면

지금은 "이렇게 제공해주는 게 많다고? 너무 좋아.." 느낌이다.

외울 필요가 뭐가 있나. 어차피 공식문서 보면 사용법 다 나오는데!!

나는 "**개굴!**" 하고 쓰기만 하면 된다.
