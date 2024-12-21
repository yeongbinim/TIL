# Lombok 설정 오류

코드 작성하는 단계에서는 하나도 문제가 없었는데, 실행하니 아래의 문제가 생겼다.

<div align="center"><img width="700" src="https://github.com/user-attachments/assets/554b2607-4d42-470c-a1ce-0529ab5a0ae7" /></div>

보자마자 Lombok 문제라는 것을 알 수 있었다. Lombok을 인식하지 못해서 컴파일을 제대로 수행하지 못한 것이다.

<br/>

### 문제 발견: build.gradle 의존성 잘못

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/13263fda-83e0-4584-b858-e31e4486dfe1"/></div>

Build, Execution, Deployment > Compiler > Annotation Processors 를 확인해봤더니 잘 켜져 있었다. 도대체 뭐가 문제지??

<div align="center"><img width="500" src="https://github.com/user-attachments/assets/a298b8b2-6bb5-411f-8562-ed23e45c4ed5" /></div>

처음 프로젝트 생성할때 롬복을 깜박하고 추가를 못해서 저 위의 `Add Starters ...` 버튼으로 추가했는데, `implementation` 으로 들어가 있었다.

프로젝트 생성할 때 롬복 추가하면 아래와 같이 달려있었는데 말이다.

```java
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

저 `annotationProcessor` 가 중요하다. 특정 어노테이션 프로세서를 컴파일 태스크에 포함시키는 역할을 한다. 

이 설정 없이는 Gradle이 롬복 어노테이션 프로세서를 자동으로 불러오지 않으므로, 어노테이션을 기반으로 한 코드 변환이 일어나지 않는 것이다.

<br/>

### 플러그인은 무슨 역할을 할까

이건 뭐지? 하면서 처음부터 그냥 깔려 있었던 Lombok 플러그인을 Disable해보았다. 

<div align="center"><img width="600" alt="스크린샷 2024-12-19 오후 8 28 46" src="https://github.com/user-attachments/assets/ac2c39a1-b565-4ee8-abb7-fbe83f97962f" /></div>

이랬더니 인텔리제이가 아예 롬복 애너테이션을 인식하지 못해서 getter나 생성자를 못가져온다. 그래서 코드 작성단계에서 문제가 생긴다. (물론 빌드에 있어서 차이는 없었다.)

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/409f195b-bed5-496c-bc40-4f93d126b750" /></div>



<br/>

### Enable annotation processing 꺼도 되는거 아냐?

그렇다면 인텔리제이의 `Enable annotaion processing` 저 옵션은 무슨의미지? 하면서 끄고 실행해 봤다.

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/6ffb818d-09bc-4d0e-b048-a81f8fb8b460" /></div>

끄고 실행해도 정상적으로 빌드가 되고, 포스트맨으로 요청을 해봐도 실행되는 것을 확인해버렸다....

멘붕.... 

<br/>

### Build and run을 인텔리제이로 변경

그런데 곰곰이 생각해보니.. 저 `Enable annotation processing`은 인텔리제이에 대한 설정이고, 나는 Gradle로 빌드가 되고 있잖아?

빌드를 인텔리제이로 하도록 변경하면 어떻게 될까? 

<div align="center"><img width="700" src="https://github.com/user-attachments/assets/400aa2af-01a7-485d-91e5-6530997d13e4" /></div>

위의 사진처럼 빌드할때 Intellij에서 빌드되게 했다.

<br/>

<div align="center"><img width="200" src="https://github.com/user-attachments/assets/5ac0acee-321f-4cfb-8cee-4cf17f883a24" /></div>

그리고 빌드 중 잠깐 위의 메시지가 나오긴 했지만 딱히 로그에 에러 문구는 없었다. 여기서 2차 멘붕...

<img width="497" alt="스크린샷 2024-12-21 오후 4 32 45" src="https://github.com/user-attachments/assets/6181298c-c5c2-42cf-9b44-f3f5d7e3d7de" />

/out 폴더에서 클래스 파일을 봤을 때에도 정상적으로 `@Getter` 가 반영되어 저렇게 메서드들이 생성 되어 있었다.

<br/>

그러나 실행하는 순간에 문제가 생겼다. JacksonMessageConverter 왈 기본 생성자가 없다고 안된다고 한다.

<img width="700" src="https://github.com/user-attachments/assets/e76f662e-10ae-4cde-b5c8-6aa4d6a81673" />

하지만, Gradle로 빌드했을 때에는 잘 됐는걸...? 또 무슨 에러지....

쨌든 이 부분은 아직 해결을 못했다.



