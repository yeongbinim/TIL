# Query DSL로 통합검색 개발하기

Shop 엔티티에 name, 그리고 Menu 엔티티에 name, description 모두 통틀어서 검색하는 기능을 개발하려 한다.

JPQL로 쿼리를 작성하는 방법도 있겠지만, 쿼리의 오류를 런타임에서만 확인이 가능하고, 엔티티 필드가 변경되면 이걸 수동으로 수정해야 하는등.. 다소 유지보수하기 힘들다.

QueryDSL은 SQL, JPQL 같은 쿼리를 Java 코드로 작성할 수 있게 해주는 타입 안전한 ORM 기반 쿼리 빌더이다.

이 쿼리 DSL을 설정하고 사용하여 검색기능 개발한 과정에 대해 적는다.

<br/>

### 목차

- [QueryDSL 사용을 위한 설정](#querydsl-사용을-위한-설정)
- [기존 Repository와 같이 쓰기](#기존-repository와-같이-쓰기)
- [검색기능 개발하기](#검색기능-개발하기)

<br/>

### QueryDSL 사용을 위한 설정

QueryDSL은 Gradle 빌드할 때 Q타입들을 뽑아내고 이걸 사용해야 하기 때문에 설정 과정이 꽤 있다. 스프링 부트 3.x를 기준으로 적어보겠다.

우선 의존성을 추가 하려 하는데, Starter에 검색해 봐도 Query DSL의 의존성을 추가하기 위한 게 나오지 않는다. 따라서 아래처럼 직접 추가해주어야 한다.

```java
{
  implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'

  annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
  annotationProcessor "jakarta.annotation:jakarta.annotation-api"
  annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}
  
clean { delete file('src/main/generated') }
```

- querydsl-apt: Querydsl 관련 코드 생성 기능 제공
- querydsl-jpa: 실제 querydsl 사용을 위한 라이브러리

마지막 줄은 빌드 프로세스 중에 생성된 파일들을 정리한다. QueryDSL은 메타 모델을 `src/main/generated` 디렉터리에 생성하는데, 이 설정은 빌드를 깨끗하게 유지하기 위해 이전에 생성된 파일들을 삭제한다.

이상태로 한번만 빌드를 하면 build 폴더에 자동으로 Q타입들이 생성되는 것을 확인할 수 있다.

그리고 JPAQueryFactory를 생성할 때에는 JPA의 EntityManager가 필요하기 때문에 아래와 같이 빈 등록을 해준다.

```java
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslConfig {

  @PersistenceContext
  private EntityManager entityManager;

  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }
}
```

이제 모든 설정 준비가 마쳤다.

이제 QueryDSL을 사용하려면 아래처럼 사용하면 된다.

```java
@RequiredArgsConstructor
public class MenuService {
  private final JPAQueryFactory queryFactory;
  
  public Menu getMenu() {
	  QMenu qMenu = QMenu.menu();
	  return queryFactory
  	  .selectFrom(qMenu)
    	.fetchOne();
  }
}
```

<br/>

### 기존 Repository와 같이 쓰기

기존에 Spring-data-jpa를 쓰고 있어서 이 JpaRepository와 같이 호환성 있게 쓸 수는 없을까?

```java
public interface MenuRepository extends JpaRepository<Menu, Long> {}
```

간단한 쿼리는 이 인터페이스에, 그리고 복잡한 쿼리는 QueryDSL로 말이다.

그러기 위해 조금의 세팅을 더 해준다.

```java
public interface MenuRepositoryCustom {
    List<Menu> searchByKeyword(String keyword);
}
```

위와 같이 인터페이스를 하나 추가하여 내가 QueryDSL을 통해 구현하고자 하는 함수를 명시해주고

```java
public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {}
```

기존에 쓰던 리포지터리가 이걸 상속받도록 한다. 참고로 인터페이스는 다중상속이 가능하다.

```java
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Menu> searchByKeyword(String keyword) {
        QMenu qMenu = QMenu.menu;
        QShop qShop = QShop.shop;

        return queryFactory.selectFrom(qMenu)
            //구현
    }
}
```

그리고 위와같이 구현해주면 된다.

<br/>



### 검색기능 개발하기

우선 Menu의 name과 description으로부터 keyword를 검색하는 sql을 만들어보자

```java
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Menu> searchByKeyword(String keyword) {
        QMenu qMenu = QMenu.menu;
        QShop qShop = QShop.shop;

        return queryFactory.selectFrom(qMenu)
            .join(qMenu.shop, qShop).fetchJoin()
            .where(qMenu.name.containsIgnoreCase(keyword)
                .or(qMenu.description.containsIgnoreCase(keyword))
                .and(qMenu.isDeleted.isFalse()))
            .fetch();
    }
}
```

위와같이 QueryDSL에서 제공해주는 함수로 쉽게 만들 수 있다.

n+1 문제를 대비하여 shop에 대해 fetch join도 적용하였다.

<div align="center"><img width="350" src="https://github.com/user-attachments/assets/04ebd5f2-326c-4550-91f7-62ef82a2dbfb" /></div>

실제 쿼리는 위와 같이 날아간다.

ShopRepository는 위의 부분에서 or문만 빼고 이름만 검색하도록 구현한 다음

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/65109132-cbb4-4fc8-aea7-a95eca025443" /></div>

위와같이 서비스계층에서 두 Repository로부터 검색을 하도록 한다.

해당 컨트롤러를 만들어서 결과를 확인해보면

<div align="center"><img width="600" src="https://github.com/user-attachments/assets/23e6066f-b93a-47db-97b6-fdc0fc3bc73c" /></div>
