# JPA 제네릭 타입에 ID 타입 넘기는 이유

```java
public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long> {}
```

이렇게 인터페이스를 정의하면, Spring Data JPA가 실행 시 이 인터페이스의 구현체를 자동으로 생성하는데, 이 과정에서 리플렉션과 프록시를 사용한다고 한다.

리플렉션을 사용할 건데 왜 굳이 id값이 Long 타입이라고 정보를 넘기는 걸까?

이미 MemberEntity를 기준으로 할거라고 테이블 정보를 넘겼고, 여기에서 리플렉션을 통해 `@Id` 가 있는 필드를 찾을게 아닌가?

<br/>

알아보니 두번째 넘기는 Id는 리플렉션에 활용되기 위한 용도가 아니라, 타입정보를 제공받기 위해서였다.

<img width="400" src="https://github.com/user-attachments/assets/bf566003-3ccf-4873-af86-6c8fe7315e82" />

위와 같이 개발단계에서 findById 와같은 메서드의 타입 정보를 제공받을 수 있으며, 만약 MemberEntity만으로 넘긴다고 할 시, 리플렉션은 런타임에 일어나기 때문에 저 타입정보를 제공받을 수 없는 것이다.

아 물론 첫번째 인자인 MemberEntity는 타입 정보 제공 뿐만 아니라, Spring Data JPA 내부적으로 리플렉션을 통한 여러 작업에 사용되며, 이걸 통해 엔티티에 대한 다양한 메타데이터를 자동으로 추출하고 관리할 수 있다.
