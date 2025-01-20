# 프록시 패턴과 데코레이터 패턴

요청하는 객체를 클라이언트라고 하면, 클라이언트가 특정 객체에게 요청을 할때, **직접적으로 호출**을 하는 것이 아니라, Proxy라는 대리자를 통해서 **간접적으로 호출**하는 방법이 프록시 패턴이다.

이 프록시라는 대리자를 거치게 되면, 다음의 이점들이 있다.

1. 접근 제어
2. 캐싱
3. 부가 기능 추가
4. 프록시 체이닝

프록시 패턴을 구현하려면 아래의 다이어그램처럼 클래스를 구성하면된다.

<div align="center"><img width="300" alt="Image" src="https://github.com/user-attachments/assets/8e36f154-2e46-4350-86f8-a2f8c64e5126" /></div>

인터페이스를 실제 **타겟**(RealSubject)과 **프록시**가 구현해서, 클라이언트는 프록시에 의존하도록, 프록시는 타겟에 의존하도록 구성하는 것이다.

이 프록시 패턴은 데코레이터와 상당히 유사한데, 둘이 해결하고자 하는 목적에 따라서 패턴의 이름이 달라진다.

**프록시 패턴은 접근 제어**가 목적이고, **데코레이터 패턴은 새로운 기능 추가**가 목적이다.

<div align="center"><img width="300" alt="Image" src="https://github.com/user-attachments/assets/5480781b-fc00-4aa4-ac94-5b7e27264a89" /></div>

사용하는 용어도 위의 그림처럼 다른데, 데코레이터 패턴은 인터페이스를 **리얼 컴포넌트**와 **데코레이터**가 구현해서, 클라이언트는 데코레이터에 의존하도록, 데코레이터는 리얼 컴포넌트에 의존하도록 구성하는 것이다.

여기에 Decorator들이 아래처럼 추가될 수가 있는데

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/7f6f96e0-4b68-4f3e-8192-0d6f970ec7a4" /></div>

이렇게 되면, Decorator마다 중복되는 로직들이 발생할 수 있다. 예를들면, 항상 꾸며줄 대상이 필요하기 때문에, 호출 대상인 component를 갖고, 이것을 항상 호출해야 한다는 점이다. 

이런 중복을 제거하기 위해 Decorator라는 추상 클래스를 만드는 방법을 고민할 수 있는데,

<div align="center"><img width="347" alt="Image" src="https://github.com/user-attachments/assets/99aa0d99-d4c8-4481-81ea-cb4764a0c6da" /></div>

여기까지 고민한 것이 GOF에서 설명하는 데코레이터 패턴의 기본 예제이다.

