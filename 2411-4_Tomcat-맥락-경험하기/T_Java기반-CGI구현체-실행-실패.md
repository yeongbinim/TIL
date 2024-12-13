# Java기반 CGI구현체 실행 실패

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
