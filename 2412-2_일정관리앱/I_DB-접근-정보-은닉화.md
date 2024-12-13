# DB 접근 정보 은닉화

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:4444/scheduler
    username: user
    password: password
```

위와 같은 중요한 정보를 github에 그냥 올릴 수가 없다.

환경 변수를 사용하는 방법도 있고, 프로파일을 이용한 설정을 분리하는 방법이 있는데, 후자를 경험 해보기로 했다.

application-env.yml 이라는 파일을 만들어서 여기로 옮기고, application.yml에는 아래의 정보를 추가했다.

```yaml
spring:
  profiles:
    include:
      - env # application-env.yml 포함함
```

이로써 github에는 db 접근 관련 정보가 올라가지 않는 것이다.

그런데, 팀원끼리 개발할 때.. 이 application-env.yml을 공유할 수는 없을까?

깃헙의 서브모듈을 활용해서 서브모듈을 private으로 하는 방법도 있다고 한다.
