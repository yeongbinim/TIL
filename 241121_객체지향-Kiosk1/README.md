# 객체지향 Kiosk1

콘솔 인터페이스로 사용자에게 음식 메뉴 정보를 제공하는 서비스를 구현하는 과제가 있었다. 아래의 기본 요구사항을 따른 뒤에, 원하는 요구사항들을 추가해 나갈 예정이다.

- 업무 요구사항
  - [x] 사용자는 콘솔에서 메뉴를 확인하고 선택할 수 있다.
  - [x] 입력은 0부터 시작해 증가하는 숫자로만 받는다.

- 기술 요구사항

  - [x] lv1:
    - 시작점이 되는 main함수를 가진 클래스는 `Main`으로 하라
  - [x] lv2:
    - 메뉴아이템은 이름, 가격, 설명 값을 가진 `MenuItem` 클래스를 활용하라
    - 이 메뉴아이템을 관리하는 List를 선언한다.
  - [x] lv3: 
    - 사용자의 입력을 처리하는 `Kiosk`클래스를 만들어라.
    - `List<MenuItem> menuItems`는 Kiosk 클래스 생성자를 통해 값을 할당한다.
    - main 함수에서 관리하던 입력과 반복문 로직은 이제 `start` 함수를 만들어 관리한다.
  - [x] lv4:
    - `List<MenuItem> menuItems`를 갖는 `Menu` 클래스를 만들어라
    - `Kiosk`는 `List<Menu> menuList`를 갖는다.

[[lv1 코드 보러가기]](./src/lv1/), [[lv2 코드 보러가기]](./src/lv2/), [[lv3 코드 보러가기]](./src/lv3/), [[lv4 코드 보러가기]](./src/lv4/)

<br/>



## lv1 ~ 4: 기본 요구사항 구현

### 다이어그램 구조

각 클래스간의 관계를 어느정도 그려보았다. MenuItem이 n개일때 Menu는 1개이고, Menu가 n개일 때, Kiosk는 1개이다.

<div align="center"><img width="600" alt="스크린샷 2024-11-21 오후 8 19 45" src="https://github.com/user-attachments/assets/75edb90c-d0c0-4d00-8fea-a0d0eb8b6e96"></div>



A → B를 'A가 B를 의존한다' 라고 하면, 일반적인 도메인 모델에서는 아래의 구조를 세운다.

<div align="center"><img width="360" alt="스크린샷 2024-11-21 오후 8 25 05" src="https://github.com/user-attachments/assets/3406a180-8d7a-4d83-a5d7-6a1d9202b67f"></div>



그래서 처음에 당연히 위와 같이 코드를 작성해나갔는데, 요구사항을 보니 아래의 구조로 설계를 하라고 한다.

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/27dfc2c4-8cbc-4513-9179-61ccb969de68" /></div>

이렇게 되면, Kiosk에는 `List<Menu>`, Menu는 `List<MenuItem>` 를 갖고 있어야 하는데..

우선 요구사항대로 이렇게 한 후에 추가 고민한 글에 대해서 적어보려 한다.


<br/>

### 가변인자

Kiosk 클래스 생성자를 통해 List에 있는 값을 할당한다는 부분에서 고민을 했는데, 가변인자가 생각 났다.

```java
public class Kiosk {
  List<Menu> menuList;

  public Kiosk(Menu ...menu) {
    menuList = List.of(menu);
  }
}
```

이런 식으로 하니 아래와 같이 여러 인자를 넣을 수 있었다.

```java
Kiosk kiosk = new Kiosk(Burgers, Drinks, Desserts);
```

굳이 배열을 생성해서 복사한다거나, 컬렉션을 전달한다거나 하는 것보다 훨씬 깔끔해진다.



<br/>



## 마치며

지금은 단순히 메뉴를 집어 넣고 조회하는 기능밖에 없어서, 흠.. 문제해결과정이랄게 딱히 없었다.

추가로 경란님이 말씀하신 콤보 할인이라던가, 서브웨이처럼 여러 토핑을 추가하는 기능.... 할인 묶음이 있을때에는 주문할 때 할인 추천도 해주는... 그런 기능도 추가해보려 한다.

