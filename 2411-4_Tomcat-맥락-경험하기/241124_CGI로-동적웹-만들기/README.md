# CGI로 동적웹 만들기

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

## 마치며

FastCGI 라던가.. 이것저것 더 해보려 했는데, Java 실행 안되는 거에서 한참을 헤매서 결국 기본적인 구현만 해봤다.

톰캣을 사용하면 어떤 이점이 있구나! 하고 알게되는게 이번 내 미션의 핵심이었다.

그리고, 직접 해보면서 확실히 웹 서버만으로 동적기능을 할 수 있다는 것은 알았다.

따라서 WS와 WAS를 나누는 기준을 단순히 동적기능이 가능하냐 여부로 따지면 안된다는 것을 알았다.

<br/>

둘을 나누는 데의 핵심은 목적에 있다.

지금 내가 이걸 정적인 자료 처리에만 쓰냐! 하면 => WS, 동적인 처리에 집중하냐! => WAS

어떤 목적으로 사용하냐가 중요한 거다. express로 정적인 자료만 처리하면 우리는 "express를 WS로 사용중이에여"할 것이다.
