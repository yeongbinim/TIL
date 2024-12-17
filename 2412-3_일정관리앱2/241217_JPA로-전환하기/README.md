# JPA로 전환하기

기존 MemoryRepository로 CRUD하고 있는 코드를 JpaRepository로 전환해보자

<br/>

### 목차
- [Entity 만들기](#entity-만들기)
- [JpaRepository 만들기](#jparepository-만들기)
- [RepositoryImpl 만들기](#repositoryimpl-만들기)


<br/>

### Entity 만들기

나는 서비스 계층을 순수 POJO기반을 유지하도록 하고 싶어서 계속해서 도메인 객체와 별개로 DB Entity를 따로 만들 것이다.

```java
@Entity
@Table(name = "member")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class MemberEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public static MemberEntity from(Member member) {
  return new MemberEntity(
  member.getId(),
  member.getName(),
  member.getEmail(),
  member.getPassword(),
  member.getCreatedAt(),
  member.getUpdatedAt()
  );
  }

  public Member toModel() {
  return new Member(id, name, email, password, createdAt, updatedAt);
  }
}

```

JPA Entity 클래스는 기본 생성자를 필요로 한다. 여기서 나는 접근 제한을 protected로 제한했는데, 그 이유를 알려면 이 생성자가 누구에 의해서 호출되는지 알아야 한다.

프록시 클래스가 이 원본 엔티티 클래스를 상속받으며, 이 프록시 클래스에 의해 생성되므로 protected 접근 제한을 걸어도 되는 것이다.

그리고, from과 같은 팩토리 메서드 패턴에서 생성하기 위해서 private 제한으로 걸어뒀다.

이외에도 사용한 애너테이션을 설명하면 아래와 같다.

- `@Entity`: 해당 클래스가 JPA 엔티티임을 나타낸다. 
- `@Table`: 엔티티가 매핑될 데이터베이스 테이블의 정보를 제공한다. `name` 속성을 통해 매핑할 테이블의 이름을 지정할 수 있고, 생략하면 클래스 이름을 테이블 이름으로 사용한다.
- `@Id`: 해당 필드가 테이블의 기본 키(primary key) 역할을 한다는 것을 나타낸다. 각 엔티티는 하나 이상의 `@Id` 필드를 가져야 하며, 유니크해야 한다.
- `@Column`: 필드가 매핑될 테이블의 컬럼에 대한 세부 정보를 설정한다.
- `@GeneratedValue`: 기본 키 필드의 값이 어떻게 자동 생성될지를 지정. `IDENTITY`, `SEQUENCE`, `AUTO`, `TABLE` 등의 전략이 있다.

<br/>

### JpaRepository 만들기

JpaRepository 인터페이스를 확장하여 아래와 같이 인터페이스를 작성했다.

```java
public interface JpaMemberRepository extends JpaRepository<MemberEntity, Long> {}
```

이렇게 인터페이스를 정의하면, Spring Data JPA가 실행 시 이 인터페이스의 구현체를 자동으로 생성한다. (이 과정에서 리플렉션과 프록시를 사용한다고 한다)

`JpaRepository<T, ID>` 에서 사용하는 제네릭 타입은 아래와 같다.

- `T`: 엔티티 클래스의 타입으로, DB 테이블과 매핑될 클래스를 넣는다. (아까 만든 MemberEntity)
- `ID`: 엔티티의 식별자 필드의 타입을 나타낸다. `findById` 와 같은 타입 정보를 제공받을 수 있다.

<br/>

### RepositoryImpl 만들기

나는 도메인 객체와 DB Entity를 구분하여 사용하고 있기 때문에, 실제 Repository 구현체를 만들어 여기서 변환하는 코드를 작성해주어야 한다.

```java
@Repository
@Primary
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

  private final JpaMemberRepository jpaMemberRepository;

  @Override
  public Member create(Member member) {
  return jpaMemberRepository.save(MemberEntity.from(member)).toModel();
  }

  @Override
  public List<Member> findAll() {
  return jpaMemberRepository.findAll().stream()
      .map(MemberEntity::toModel).toList();
  }

  @Override
  public Optional<Member> findById(Long id) {
  return jpaMemberRepository.findById(id).map(MemberEntity::toModel);
  }

  @Override
  public Member update(Member member) {
  return jpaMemberRepository.save(MemberEntity.from(member)).toModel();
  }

  @Override
  public void delete(Long id) {
  jpaMemberRepository.deleteById(id);
  }
}
```

이럴거면 ORM 왜 써? 라고 할 수 있지만.. 이로써 도메인 로직과 DB 접근 기술을 명확하게 분리해 냈다. 실제 프로젝트에서는 DB 엔티티와 도메인 객체를 구분하려 하지 않을 것 같기는 하다...ㅎㅎ

그 이유는 DB엔티티와 도메인 객체의 변경 주기가 같고, JPA 기술을 활용하기로 결정한 상황에서 DB가 바뀌는 일은 없을 것이기 때문이다.
