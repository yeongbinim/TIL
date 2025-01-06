# JWT secret 최소 길이

<img width="1000" src="https://github.com/user-attachments/assets/240d0d11-4260-4911-b806-c422a7fee770" />

프로젝트를 처음 내려받아 실행하는데 위와같은 에러가 났다. filterConfig를 생성하는 과정에서 에러가 났는데, 그 이유는 jwtUtil를 생성하는 과정에서 에러가 났기 때문이고, 그 이유는 jwt.secret.key 를 resolve 하는 과정에서 에러가 났기 때문이란다.

<img width="1000" src="https://github.com/user-attachments/assets/6d12d115-f986-4ace-9029-04df83ed0556" />

마지막 에러를 확대해보면 위와 같다.

이제 JwtUtil로 들어가서 문제를 파악해보자

<img width="600" src="https://github.com/user-attachments/assets/7c3ec722-1318-44a2-8994-c0773bd2a204" />

저 `@Value`는 application.properties에서 프로퍼티 값을 읽어서 값을 주입할 수 있도록 해주는 애너테이션인데, 내가 application.properties 저 jwt.secret.key를 선언하지 않았기 때문에 발생한 에러였다.



<img width="1000" src="https://github.com/user-attachments/assets/9133c29f-3a35-45d3-a246-1aed048f7120" />

jwt.secret.key 에 hello라는 값을 지정하고 실행했더니, 다시 에러가 발생했다.

마지막 줄을 해석해보면

```
지정된 키 바이트 배열은 40비트로, JWT HMAC-SHA 알고리즘에 충분히 안전하지 않습니다.
JWT JWA 사양(RFC 7518, 섹션 3.2)은 HMAC-SHA 알고리즘에 사용되는 키의 크기가 >= 256비트여야 한다고 명시하고 있습니다(키 크기는 해시 출력 크기 이상이어야 함).
```

로, 보안상 암호화 알고리즘에 사용되는 키의 크기가 너무 짧아서 에러가 발생한 것이었다.

현재 hello5글자 즉, 5byte(= 40bit)밖에 안되기 때문에 이를 최소 32자까지 늘려야 했다.

<br/>

결론적으로 임의의 32글자를 설정했더니 정상적으로 돌아가는 것을 확인할 수 있었다.