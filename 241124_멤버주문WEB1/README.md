# 회원 주문 WEB #1 | CGI로 동적웹 만들기

Spring Web MVC를 사용하여 애플리케이션을 실행하려면, 톰캣과 같은 외부 Servlet 컨테이너에 배포해야 한다고 한다. (물론 Spring Boot는 이 톰캣이 기본적으로 내장되어 있다)

클라이언트가 요청하는 리소스를 정적으로 응답하는 정적웹과는 다르게, 톰캣을 사용하면 동적으로 처리하여 응답할 수 있다고 하는데, 그렇다면 Web Server만으로는 동적인 서비스를 제공하지 못하냐? 하면 그것은 아니라는 건 알게 되었다.

CGI라는 표준을 지키는 스크립트를 작성한다면 Web Server가 이 스크립트를 호출하여 동적 컨텐츠를 생성할 수 있다.

그래서 직접 해보고 어떤 단점이 있는지 확인하고 싶어서 아래와 같은 요구사항을 작성했다.

- 기능 요구사항
  - [x] 사용자를 추가할 수 있다.
  - [x] 사용자들을 조회할 수 있다.
- 기술 요구사항
  - [x] Apache 웹 서버를 사용할 것 (WAS 금지)
  - [x] CGI 구현체는 Java기반으로 작성할 것



<div align="center"><img src="https://github.com/user-attachments/assets/0b8f7b30-9ea1-4058-b7e5-1e61a8f67b3d"/></div>



<br/>

## 목차

- [WebServer와 CGI구현체로 동적 웹 만들기](#webserver와-cgi구현체로-동적-웹-만들기)
  - [Apache 설치 및 시작](#apache-설치-및-시작)
  - [CGI 활성화](#cgi-활성화)
  - [Java로 CGI구현체 작성](#java로-cgi구현체-작성)
  - [CGI Wrapper 스크립트 작성](#cgi-wrapper-스크립트-작성)
  - [CGI 구현체의 문제점](#cgi-구현체의-문제점)
- [트러블 슈팅](#트러블-슈팅)
  - [Java 코드 실행 불가](#java-코드-실행-불가)
  - [CGI 구현체는 상태 유지가 안됨](#cgi-구현체는-상태-유지가-안됨)
- [마치며](#마치며)

<br/>

## WebServer와 CGI구현체로 동적 웹 만들기

### Apache 설치 및 시작

- Apache 설치

  ```SHELL
  brew install httpd
  ```

  - Homebrew에서 Apache는 `httpd`라는 이름으로 제공된다.

- Apache 서비스 시작

  ```
  sudo brew services start httpd
  ```

  - 이외의 명령어 종류:
    - `sudo brew services list`: 정상적으로 실행됐는지 확인
    - `brew info httpd`: Apache 기본 설정 파일 위치 확인 및 Document Root 확인
    
      <img width="568" alt="스크린샷 2024-11-25 오후 2 53 21" src="https://github.com/user-attachments/assets/b2592ba4-d9e1-4b23-87ee-ed9d737a9933">
    - `sudo brew services stop httpd`: Apache 서비스 종료
    - `brew services restart httpd`: Apche 서비스 재시작

위의 brew info httpd를 입력시 열린 포트가 나오는데 해당 포트로 접속해보면 (나같은 경우 `localhost:8080`) 로 접속해보면 It works! 하고 나온다.

그렇다면 Document Root가 있는 위치로 이동하여 해당 html 파일을 수정하면 다른 문구가 나오는 것을 확인하면 된다.

<br/>

### CGI 활성화 

Apache 설정 파일 `httpd.conf`를 열어서 CGI를 활성화해야 한다.

```shell
vim /opt/homebrew/etc/httpd/httpd.conf
```

를 통해 vim 에디터로 수정을 해보자

```shell
# LoadModule cgi_module lib/httpd/modules/mod_cgi.so
```

httpd.conf 파일에 이 위의 줄이 주석처리 되어있으면 주석을 제거한다.

그리고, 아래의 부분을 추가하거나 수정한다. (CGI 스크립트가 위치할 디렉토리를 추가하는 것이다)

```shell
ScriptAlias /cgi-bin/ "/opt/homebrew/var/www/cgi-bin/"

<Directory "/opt/homebrew/var/www/cgi-bin">
    Options +ExecCGI
    AddHandler cgi-script .class
    Require all granted
</Directory>
```

여기까지 하면 재시작 명령어를 통해 재시작 한다.

<br/>

### Java로 CGI구현체 작성

이제, 웹 서버가 실행시킬 CGI구현체를 Java로 작성해볼 것이다.

[[Java 코드 보기]](./src)

코드 구조는 아래와 같이 했다.

```
cgi-bin/
 ├── member_form.java
 ├── member_list.java
 ├── member_save.java
 ├── Member.java
 └── MemberRepository.java
```

`member_form`, `member_list`, `member_save`가 호출시 html을 반환하는 구현체가 있으며, 그 중 `member_list` 코드를 보면 아래와 같다.

```java
import java.io.*;
import java.util.*;

public class member_list {
  public static void main(String[] args) throws IOException {
    MemberRepository memberRepository = MemberRepository.getInstance();
    List<Member> members = memberRepository.findAll();

    System.out.println("Content-Type: text/html\n");

    StringBuilder tableRows = new StringBuilder();
    for (Member member : members) {
      tableRows.append("""<tr><td>""" + member.getId() + """</td>
        <td>""" + member.getName() + """</td>
        <td>""" + member.getPassword() + """</td></tr>""");
      }

    System.out.println("""
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
      </head>
      <body>
        <h1>Member List</h1>
        <a href="/cgi-bin/dispatcher/member_form">Create New Member</a>
        <table border="1">
        <thead>
          <tr><th>ID</th><th>Username</th><th>Password</th></tr>
        </thead>
        <tbody>
          """ + tableRows.toString() + """
        </tbody>
        </table>
      </body>
    </html>
    """);
  }
}

```

repository로부터 member들을 찾아서 그것에 맞게 태그를 완성하고, html로 반환하는 코드이다.

간단히 요약하면 웹 서버는 HTTP 요청 정보를 CGI 프로그램에 환경 변수(`REQUEST_URI`, `QUERY_STRING` 등)와 **표준 입력(STDIN)을 통해 전달**되고, 웹 서버는 CGI 프로그램을 실행하고, 프로그램의 **표준 출력(STDOUT)**을 기다린다.

여기서 CGI 프로그램이 표준 출력에 데이터를 출력하면, 웹 서버가 이를 수신하고, 웹 서버는 받은 데이터를 HTTP 응답으로 포장하여 클라이언트(웹 브라우저)로 전송한다.

이런 이유로 System.out.println으로 html을 작성했다.

<br/>

### CGI Wrapper 스크립트 작성

각 요청에 대해 내가 작성하여 컴파일한 class 파일을 실행시키도록 아래와 같이 작성했다.

```shell
# 요청 URL에서 경로 추출
SCRIPT_NAME=$(basename "$REQUEST_URI")

# Java 클래스 이름 결정
case "$SCRIPT_NAME" in
  member_list)
    CLASS_NAME="member_list"
    ;;
  member_save)
    CLASS_NAME="member_save"
    ;;
  member_form)
    CLASS_NAME="member_form"
    ;;
    *)
    # 잘못된 경로인 경우 404 응답
    echo "Content-Type: text/plain"
    echo
    echo "404 Not Found: $SCRIPT_NAME"
    exit 1
    ;;
  esac
$JAVA_PATH -cp "$CGI_BIN_PATH" "$SCRIPT_NAME"
```

url 파라미터를 넘겨받아 해당 이름에 맞게 내 클래스 파일을 실행시킨다.

별거 아닌거처럼 보였지만, 3시간 넘게 걸렸다.. 아래 트러블 슈팅에서 확인해보자


<br/>


### CGI 구현체의 문제점

어떻게 각 요청에 대해 이 CGI 스크립트가 실행될까? 생각했을때 현재로써는 아무리 생각해도 요청마다 프로세스를 생성하는 방법 뿐이었다.

생각이 맞을지 확인하기 위해 매 Wrapper 스크립트 맨 아래에 다음 코드를 추가했다.

```shell
echo "Process ID: $$"
```

<img width="658" alt="스크린샷 2024-11-26 오전 12 48 26" src="https://github.com/user-attachments/assets/ff37fa47-7bfd-40a3-8927-63f7a18d9e78">

결과는 역시나 요청에 대해 각각 다른 프로세스를 생성하고 있었다.

<div align="center"><img width="500" alt="스크린샷 2024-11-25 오후 11 54 18" src="https://github.com/user-attachments/assets/972228bd-879f-4fd3-a333-d5ddfade8eaa"></div>

위의 그림처럼 요청 한 번에 Web Server에서 외부 프로그램을 호출하기 위해 프로세스가 새로 생성된다.

매번 프로세스를 생성하고 종료하는 작업은.. 꽤나 비효율적이다. 프로세스를 생성하면서 메모리, CPU 리소스를 할당하고, 다양한 리소스를 초기화해야 하기 때문이다.

많은 요청이 동시에 들어오게 되면, 서버가 금방 터지고 말 것이다.

물론, 이 문제점을 극복하기 위해서 프로세스를 생성하는 방식이 아닌, 멀티 스레딩으로 하는 방법으로 개선할 수 있을 것이다. 아래처럼.

<div align="center"><img width="500" alt="스크린샷 2024-11-26 오전 12 33 31" src="https://github.com/user-attachments/assets/d4398d02-5342-4256-8fc3-1d1990a1961c"></div>

하지만 여전히 아쉬운 부분은 다른 요청이라면 저 CGI 구현체의 인스턴스가 매번 새로 생성된다는 것이다.

이를 해결하기 위해 싱글턴으로 하나의 구현체만 관리하도록 싱글턴 컨테이너를 만드려고 할 수 있을텐데... 결국 이것들을 지원해주는게 지금의 Servlet 기술이었던 것이다.

<div align="center"><img width="550" alt="스크린샷 2024-11-26 오전 12 43 43" src="https://github.com/user-attachments/assets/70fc8790-19e0-4e68-a662-000253ad825c"></div>

역시 톰캣... 최고야..

저 과정들을 한 번 겪어볼까... 하다가 오늘 너무 많이 헤매서 그림으로 대충 떼우려고 한다.

<br/>

## 트러블 슈팅

### Java 코드 실행 불가

트러블 슈팅 시간 총 3시간....

Java기반 CGI 구현체를 실행해보려 하는데, 처음에는 그냥 500에러만 나길래 알아보다가 내가 JAVA 경로를 

```shell
which java
```

로 쳐서 나온 `/usr/bin/java` 를 열심히 실행시키고(그리고 실행 권한도 전부 열어둠 755로) 있는게 잘못된 걸 알았다. 하지만 이 명령어는 실제 java 실행파일이 위치한 곳이 아니다.

현재 환경의 PATH 변수를 기반으로 실행 가능한 Java바이너리 파일 즉, 터미널에서 java 명령어를 실행했을 때 어떤 경로에서 실행 파일을 찾는지 알려주는 것이다.

따라서 여기서 나오는 /usr/bin/java는 심볼릭 링크일 가능성이 높으며, 단순히 PATH에서 실행 가능한 명령을 찾는 역할이었을 뿐이다.

진짜 java 실행파일이 어디있는지 찾기위해 아래의 명령어를 입력했다.

```shell
/usr/libexec/java_home -V
```

macOS에서 JAVA 설치 경로를 관리하고 찾는 데 사용되는 Apple 제공 유틸리티이며, java가 설치된 경로들이 나온다. 아래의 경로가 나왔다.

```
/Users/yeim/Library/Java/JavaVirtualMachines/openjdk-23.0.1/Contents/Home
```

이걸 기반으로 java가 Apache에서 정상적으로 실행되는지 아래의 cgi 스크립트를 작성하여 실행해보았다. 정상적으로 자바 버전이 나오기를 기원하면서!

```shell
export JAVA_HOME=/Users/yeim/Library/Java/JavaVirtualMachines/openjdk-23.0.1/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "JAVA_HOME=$JAVA_HOME"
echo "PATH=$PATH"

$JAVA_HOME/bin/java -version 2>&1
```

어림도 없다. 아래의 결과가 나왔다.

<img width="1237" alt="스크린샷 2024-11-25 오후 7 09 59" src="https://github.com/user-attachments/assets/9774484f-d48e-43a3-9c8b-c98a8181b029">

허가되지 않는다. 이유는 무엇일까.

분명 권한도 755로 열어줬는데...

한참을 알아보다가 상위 디렉터리의 권한들 모두 열어줘야 했다.

<div align="center"><img width="512" alt="스크린샷 2024-11-25 오후 7 26 28" src="https://github.com/user-attachments/assets/c8509214-2c00-458c-ad5b-37a92ce8a3ea"></div>

열심히 하나하나 확인 후 +x를 열어 주었다.

그동안 인텔리제이에서는 왜 잘 됐을까? 그건 소유자였으니깐..

이 Apache를 통해 실행시키면 무조건 www라는 사용자로 실행이 되더라..

--

결국 총 3시간 넘게 삽질... 왜 이렇게 오래 걸렸을까

1. 실행이 안되길래 왜 안되는지 로그 찾는데만 오래 걸렸다.
2. Apache 설정 파일인 httpd.conf에 대해서 잘 모른다는 생각에 이거 자꾸 알아보다가 시간 오래 걸렸다. (결국 여기에 문제는 없었다.)
3. Java 실행파일의 원래 위치를 몰랐는데, 열심히 이 심볼릭 링크 권한만 열어두고 왜 안되지.. 했다.
4. gpt의 도움으로 원래 위치를 알아냈지만, 상위 디렉터리의 실행 권한이 닫혀있다면 하위 디렉터리의 실행권한도 막히는 걸 몰랐다.

앞으로는 문제를 어떻게 해결해야 더 빠르게 해결할 수 있을까?

문제를 만났을때 현재 어떤 상황인지 글로 써봐야 될 것 같다.

예를 들면 "Permission denied가 났다 어떤 경우에 Permission Denied가 났는지 확인해보자. ~~경우에 난다고 한다 하나씩 확인해보자" 하면서 근본적인 에러의 이유를 알아내려고 논리적으로 접근해야 할 것 같다.

<br/>

### CGI 구현체는 상태 유지가 안됨

처음에는 MemberRepository를 아래처럼 작성했었다.

```java
public class MemberRepository {
    private static final Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;

    public static MemberRepository getInstance() {
        return new MemberRepository();
    }

    private MemberRepository() {}

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
}
```

하지만 회원을 아무리 생성해도 회원이 조회가 안되는 놀라운 일이 생겼다.

그 이유는 저 MemberRepository 자체가 메모리에 계속 남아있는게 아니라 해제되고 새로운 요청이 오면 또 새로 만들어지고... 그러기 때문에..

왜 안되는거야!!! 하면서 이것도 10분 넘게 헤매긴했다 ㅋㅋㅋㅋ 지금 생각해보면 참 바보....

따라서 CSV에 저장하고 조회하는 코드로 바꿨다. 궁금하면 코드를 살펴보자.

<br/>



## 마치며

FastCGI 라던가.. 이것저것 더 해보려 했는데, Java 실행 안되는 거에서 한참을 헤매서 결국 기본적인 구현만 해봤다.

톰캣을 사용하면 어떤 이점이 있구나! 하고 알게되는게 이번 내 미션의 핵심이었다.

그리고, 직접 해보면서 확실히 웹 서버만으로 동적기능을 할 수 있다는 것은 알았다.

따라서 WS와 WAS를 나누는 기준을 단순히 동적기능이 가능하냐 여부로 따지면 안된다는 것을 알았다.

<br/>

둘을 나누는 데의 핵심은 목적에 있다.

지금 내가 이걸 정적인 자료 처리에만 쓰냐! 하면 => WS, 동적인 처리에 집중하냐! => WAS

어떤 목적으로 사용하냐가 중요한 거다. express로 정적인 자료만 처리하면 우리는 "express를 WS로 사용중이에여"할 것이다.
