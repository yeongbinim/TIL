# HQL Parsing 에러

QueryDSL을 통한 검색 기능에 FULLTEXT 검색을 도입했다.

MySQL 테이블에는 이미 FULLTEXT 인덱스를 생성해 두었고, 쿼리에 match against 문을 사용하면 된다.

```java
@Override
public List<Menu> searchByKeyword(String keyword) {
  QMenu qMenu = QMenu.menu;
  QShop qShop = QShop.shop;

  BooleanExpression template = Expressions.booleanTemplate(
    "match({0, 1}) against ({2} in boolean mode) > 0", 
    qMenu.name, 
    qMenu.description,
    keyword
  );

  return queryFactory.selectFrom(qMenu)
      .join(qMenu.shop, qShop).fetchJoin()
      .where(qMenu.isDeleted.isFalse()
          .and(template))
      .fetch();
}
```

QueryDSL의 TemplateExpression을 통해서 이렇게 작성하면 되지 않을까? 하고 실행시켜보니

<img width="1115" alt="image" src="https://github.com/user-attachments/assets/e9ec6d97-17eb-4e20-86ef-3a074de6bff4" />

Hibernate에서 hql로 parsing을 하는 과정에서 문제가 생긴다고한다.

<br/>

이대로 네이티브 쿼리를 써야하나.. 아니!

직접 match against문을 사용하면 안된다는 걸 알았고, 직접 방언(Dialect)를 등록해서 사용해야겠다!

같은 문제를 겪은 사람들의 블로그를 찾아보니

```java
public class MySQLDialectConfig extends MySQL8Dialect {
  public MySQLDialectConfig() {
    super();

    this.registerFunction("function_test", new StandardSQLFunction("function_test", new StringType()));
  }
}
```

위와같은 방법으로 Dialect를 등록하면 된다는 글들이 대부분이었다.

<div align="center"><img width="421" alt="image" src="https://github.com/user-attachments/assets/466ff9d4-9874-45de-95c1-1c843030490d" /></div>

하지만 deprecated되었다.

일단 제쳐두고 돌아가는 코드라도 확인해보려고 쓰려 했지만

<div align="center"><img width="788" src="https://github.com/user-attachments/assets/d6df4b44-d587-4aeb-9a50-624432bb9036" /></div>

애초에 registerFunction자체를 쓸 수가 없다.

https://discourse.hibernate.org/t/hibernate-6-quarkush2dialect-registerfunction/7924

하이버네이트 커뮤니티에서 관련 문제에 대한 여러 글들을 보다가 아래의 댓글을 확인했다.

<div align="center"><img width="400" alt="스크린샷 2025-01-10 오후 10 37 29" src="https://github.com/user-attachments/assets/4f7fdf7b-9574-436a-a8eb-53c81824e927" /></div>

Hibernate 팀원인 beikov님이 FunctionContributor를 사용하라고 한다.

그래서 찾아보니 FunctionContributor 인터페이스를 구현하면 사용자 정의 함수를 Hibernate에 등록 가능하다는 걸 알았고,

이를 통해 내가 사용하고자 하는 MATCH() AGAINST() 함수를 JPA나 QueryDSL에서 마치 내장 함수처럼 사용할 수 있게 된다는 것을 알았다.

<img width="716" alt="401750477-497d02ac-9a0c-40a6-8a9c-b049f87544fe" src="https://github.com/user-attachments/assets/f1c3f0b5-0def-4f1e-bf5a-ba5875c9d775" />

따라서 위와같이 FunctionContributor를 구현한 CustomFunctionContributor를 만들고, 

`resources/META-INF/services/org.hibernate.boot.model.FunctionContributor` 파일을 생성하여 해당 FunctionContributor 경로를 등록해주었다.

<img width="800" src="https://github.com/user-attachments/assets/b88544f6-a8d5-4140-b646-644816e364e9" />

그리고 아래와 같이 수정을 했고

<img width="620" alt="401750306-1a0ca326-f6fc-48aa-af45-80f96ec418a8" src="https://github.com/user-attachments/assets/8b878e65-540b-4df2-a5ff-ff1d8829bea3" />

이걸 QueryDSL에 적용하니 정상적으로 쿼리가 잘 날아가서 검색이 되는 것을 확인할 수 있었다.

<div align="center"><img width="350" src="https://github.com/user-attachments/assets/caa8b5b0-339d-4c61-9634-8283f0c1c63a" /></div>