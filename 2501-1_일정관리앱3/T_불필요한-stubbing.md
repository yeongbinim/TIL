# 불필요한 stubbing

리팩터링이 완료된 이후 테스트코드를 돌렸더니 아래의 에러가 발생했다.

<img width="1000" src="https://github.com/user-attachments/assets/ef0ad580-632e-465e-bb73-8a75d189c8ad" />

Unnecessary stubbings detected.

즉, 사용하지 않은 더미 메서드 설정이 감지되어서 Mockito에서 UnnecessaryStubbingException을 발생시킨 것이다.

하지만 처음 이 에러를 보고 '어디에 사용되지 않은 stubbing이 있다는 거지?' 하면서 꽤 오래 헤맸다.

<img width="500" src="https://github.com/user-attachments/assets/4ea69c79-71ba-42ec-a17a-e1500b8f427f" />

Service 코드를 들어가고 나서야 리팩터링 중에 내가 todo Repository의 findById 메서드가 굳이 필요하지 않을거 같아서 뺀 부분을 발견했다.

findById 를 stubbing하는 코드를 제거했더니 정상적으로 테스트 코드가 수행이 되었다.

'아니 왜 어떤 stubbing이 불필요한지 구체적으로 안알려주는거야?' 하면서 혼자 분했지만, 에러 메시지를 다시 살펴보니

`Following stubbings are unnecessary (click to navigate to relevant line of code):` 라고 친절하게 몇번째 줄에서 문제가 생기는지 링크까지 걸어준 것을 확인했다.

에러 메시지 잘 읽어보자! 개발자를 위해서 친절하게 적어준 메시지니깐!

