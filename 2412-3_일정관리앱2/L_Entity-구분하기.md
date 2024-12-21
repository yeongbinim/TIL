# Entity 구분하기

Entity라는 개념이 DB를 배울때도, ORM을 사용할때도, 객체지향에서도 사용되는 말인데... DB 엔티티, 도메인 엔티티, 영속성 객체... 이 말들이 혼합되면서 정리할 필요를 느끼게 되었다.

> An entity is a lightweight persistence domain object. Typically, an entity represents a table in a relational database, and each entity instance corresponds to a row in the table. The primary programming artifact of an entity is the entity class, although entities can use helper classes.
>
> [[Oracle help center, "Understanding Entities", (accessed February 4, 2023)]](https://docs.oracle.com/middleware/1212/toplink/OTLCG/entities.htm)

오라클 공식 가이드를 보면, 엔티티를 영속성 도메인 객체라고 표현한다. 엔티티를 JPA와 엮어서 설명한 것이다.

하지만, 오라클이 java를 운영하고 있기도 하지만, DB도 같이 운영하고 있기 때문에 도메인 엔티티와 DB 엔티티의 개념을 혼합해서 설명했다고 생각한다.

엔티티는 JPA없이 설명이 안되는 개념이 아니며, 도메인 엔티티와 DB 엔티티는 다르다.

> 경력을 더할수록 도메인 모델에 대한 이해가 쌓이면서 실제 도메인 모델의 엔티티와 DB 관계형 모델의 엔티티는 같은 것이 아님을 알게 되었다.
>
> [최범균, DDD START! 도메인 주도 설계 구현과 핵심 개념 익히기, (지앤션, 2018-07-01), 55p]

- **도메인 엔티티**: 소프트웨어에서 어떤 도메인이나 문제를 해결하기 위해 만들어진 모델. 비즈니스 로직을 들고 있고, 식별 가능하며, 일반적으로 생명 주기를 갖는다. 사실상 도메인 객체랑 혼용돼서 쓰이는 용어라고 보면 된다.
- **DB 엔티티**: 데이터베이스에 표현하려고 하는 유형, 무형의 객체로써 서로 구별되는 것을 뜻한다.



객체지향 분야와 DB 분야에서 비슷한 고민을 갖고 같은 목적을 해결하기 위해 둘 다 엔티티 개념을 사용해 왔는데, 그 고민의 해결책이 매우 유사하면서도 달랐다.



객체지향 진영에서는 그게 클래스로 표현이 되었고, DB 진영에서는 테이블로 표현이 된 것이다.



하지만 실세계에서 서비스를 만들려면 양쪽이 협업을 해야되고, 그래서 DB 엔티티에 있는 값을 도메인 엔티티로 옮겨야 할 필요가 있는데 그게 **영속성 객체고 ORM**이다. 

<br/>

정리하면, **도메인 엔티티는 비즈니스 영역을 해결하는 모델, 영속성 객체는 ORM, DB 엔티티는 RDB에 저장되는 객체**인 것이다.
