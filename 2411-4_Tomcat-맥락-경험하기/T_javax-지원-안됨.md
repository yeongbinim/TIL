# javax는 지원하지 않음

Maven 레포지터리에서 신나게 servlet api 의존성 어떻게 추가하는지 찾아서 처음에는 

```
implementation 'javax.servlet:javax.servlet-api:5.0.0'
```

이걸 추가했었는데, 아래의 오류가 났다.

<img width="957" alt="스크린샷 2024-11-26 오후 6 36 55" src="https://github.com/user-attachments/assets/85f41b32-829a-4cf0-a1e2-c676cfa3e2a3">

뭐가 문제인지 몰라서 GPT의 도움을 받아 톰캣 9 버전부터 `javax.*` 패키지 네임스페이스가 `jakarta.*` 네임스페이스로 변경되기 시작했다고 한다.

Jakarta EE가 나오면서 Java EE 기술의 소유권이 Oracle에서 Eclipse Foundation으로 이전되면서 발생한 변화라고 한다. 자세한 건 잘 모르겠지만, 참고참고
