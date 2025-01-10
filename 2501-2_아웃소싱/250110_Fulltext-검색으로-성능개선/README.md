# FULLTEXT 검색으로 성능개선 하기

Shop 엔티티에 name, 그리고 Menu 엔티티에 name, description 모두 통틀어서 검색하는 기능이 있는데

내부적으로 보내지는 쿼리를 확인해보면,

<div align="center"><img width="350" src="https://github.com/user-attachments/assets/04ebd5f2-326c-4550-91f7-62ef82a2dbfb" /></div>

like문을 통해서 '%키워드%' 이렇게 쿼리가 만들어진다.

데이터가 적은 지금 상황에서는 괜찮지만, Menu가 많아지고 그만큼 검색해야 하는 양이 많아진다면 분명 성능에 문제가 생길 것 같았다.

따라서 성능을 측정해보고, 이것들을 비교해보기로 했다.

<br/>

### 목차

- [성능 테스트를 위한 데이터 준비](#성능-테스트를-위한-데이터-준비)
- [첫 성능 테스트 결과](#첫-성능-테스트-결과)
- [FULLTEXT INDEX 생성하기](#fulltext-index-생성하기)
- [QueryDSL로 커스텀 반영하기](#querydsl로-커스텀-반영하기)
- [최종 결과](#최종-결과)


<br/>

### 성능 테스트를 위한 데이터 준비

우선 테스트 결과를 그래도 쉽게 파악하기 위해서 DB에 가짜 데이터들을 넣어두어야 했다.

데이터 처리 관련해서는 python 만한게 없다고 생각한다.

우선 가상환경 실행해서 아래의 모듈을 설치한다.

```shell
$ pip install faker sqlalchemy pandas
```

그리고 아래의 스크립트를 작성해 우선 csv형식으로 저장되게 했다.

```python
from faker import Faker
import pandas as pd

fake = Faker()

# User 데이터 생성
def generate_users(n):
    users_data = [{
        'created_at': fake.date_time_this_decade(),
        'email': fake.email(),
        'password': fake.password(),
        'username': fake.user_name(),
        'user_role': fake.random_element(elements=('OWNER', 'USER'))
    } for _ in range(n)]
    return pd.DataFrame(users_data)

def generate_shops(users_count, shops_per_user):
    # 중략, fake를 통한 shop_data세팅
    return pd.DataFrame(shops_data)

def generate_menus(shops_count, menus_per_shop):
    # 중략, fake를 통한 menus_data세팅
    return pd.DataFrame(menus_data)

  
users_df = generate_users(5000) # 사용자 5000명
shops_df = generate_shops(5000, 3)  # 각 사용자당 3개의 상점
menus_df = generate_menus(15000, 20)  # 각 상점당 20개의 메뉴

# CSV로 저장
users_df.to_csv('users.csv', index=False)
shops_df.to_csv('shops.csv', index=False)
menus_df.to_csv('menus.csv', index=False)
```

그리고 코드 실행 결과 3개의 csv파일이 만들어졌고

<img width="1000" src="https://github.com/user-attachments/assets/de775882-3e70-4174-a09a-7a1257cbf5e4" />

이번에는 이 csv파일 3개를 database 서버에 전송하도록 했다.

```python
import pandas as pd
from sqlalchemy import create_engine

# MySQL 데이터베이스 연결 설정
engine = create_engine("mysql+pymysql://user:password@localhost:4444/outsourcing?charset=utf8mb4")

# CSV 파일에서 데이터를 읽어옵니다.
def load_data(file_path):
    return pd.read_csv(file_path)

# 데이터를 MySQL 데이터베이스에 삽입합니다.
def insert_data(dataframe, table_name):
    dataframe.to_sql(table_name, con=engine, if_exists='append', index=False)

# 사용자 데이터 삽입
users = load_data('users.csv')
insert_data(users, 'users')

# 상점 데이터 삽입
shops = load_data('shops.csv')
insert_data(shops, 'shops')

# 메뉴 데이터 삽입
menus = load_data('menus.csv')
insert_data(menus, 'menus')
```

mysql 서버에 접속해서 데이터를 확인해보니 정상적으로 들어온 것을 확인할 수 있었다.

<img width="250" src="https://github.com/user-attachments/assets/0364023b-ac9a-48cd-8268-0e45fb1449c2" />

<br/>

### 첫 성능 테스트 결과

별도의 성능 테스트 툴을 사용하지 않고 아래처럼 Postman으로 요청을 보냈다.

<img width="1000" src="https://github.com/user-attachments/assets/6083e916-f02d-4108-92e8-23b20d36b7fd" />

그리고 첫번째 거는 버리고, 그 뒤로 두 번을 평균 내기로 했다.

<div align="center"><img width="590" src="https://github.com/user-attachments/assets/96b05cc2-5f77-41ac-b142-b464cd240e50" /></div>

위에처럼 평균 1.6초가 걸리는 것을 확인했다.



좀 더 줄일 수 있지 않을까? 그래도 1초 미만은 나왔으면 좋겠는데..



<br/>

### FULLTEXT INDEX 생성하기

전체 텍스트 검색 (FULLTEXT 검색)은 첫 글자 뿐 아니라 중간의 단어나 문장으로도 인덱스를 생성해줘서 전체 텍스트 인덱스를 통해 더 빠르게 검색 결과를 얻을 수 있다고 한다.

MySQL 5.5부터 InnoDB 스토리지 엔진을 사용하는데, 이 InnoDB에서 풀 텍스트 인덱스가 지원되기 때문에 현재 MySQL 8.0 을 사용하고 있어 이 방법이 적합하다고 판단했다.

인덱스를 생성하기 전에 EXPLAIN문으로 현재 쿼리의 실행 계획을 확인해봤다.

<img width="1118" alt="스크린샷 2025-01-10 오후 3 51 21" src="https://github.com/user-attachments/assets/1424312e-b3b5-4413-9a69-5a9735b39c32" />

테이블 전체 스캔을 하고 있고, 인덱스를 사용되지 않아 294,150개의 행을 읽고 있다.



<div align="center"><img width="400" src="https://github.com/user-attachments/assets/baba4650-c8b1-4294-821c-7c48dcb43368" /></div>

위의 쿼리로 shops와 menus에 각각 인덱스를 생성해주었고,

<img width="1404" alt="스크린샷 2025-01-09 오후 8 25 28" src="https://github.com/user-attachments/assets/9f3bb8a0-3093-4a7f-92aa-c7dd384ac158" />

<img width="1402" alt="스크린샷 2025-01-09 오후 8 25 42" src="https://github.com/user-attachments/assets/bcbea6c9-8714-4152-b555-18bb510abe25" />

풀텍스트 인덱스가 잘 생성된 것을 확인할 수 있었다.

그 이후 다시 EXPLAIN문으로 쿼리 실행 계획을 확인해보니

<img width="1039" alt="스크린샷 2025-01-10 오후 4 00 18" src="https://github.com/user-attachments/assets/378f7013-b3f6-4d43-8855-88ff4d89c7e0" />

`MATCH AGAINST` 쿼리가 풀텍스트 인덱스를 효과적으로 사용하고 있어, 매우 적은 수의 행(1행)만을 검사하고 있음 확인할 수 있다. 인덱스가 잘 작동하고 있으며 쿼리 성능이 최적화되었음을 의미한다.

<br/>

### QueryDSL에서 MATCH, AGAINST 함수 사용하기

`MATCH()`,  `AGAINST()` 함수를 사용해야지만 풀텍스트 검색이 가능한데, 이 함수는 JPA가 기본적으로 지원하지 않기 때문에, 직접 SQL 쿼리를 작성하여 이 기능을 구현해야 했다.

그러나 FunctionContributor 인터페이스를 구현하면 사용자 정의 함수를 Hibernate에 등록 가능하고, 이를 통해 MATCH() AGAINST() 함수를 JPA나 QueryDSL에서 마치 내장 함수처럼 사용할 수 있게끔 하려 한다.

```java
public class CustomFunctionContributor implements FunctionContributor {
    private static final String FUNCTION2_NAME = "match_2params_against";
    private static final String FUNCTION2_PATTERN = "MATCH (?1, ?2) AGAINST (?3 in boolean mode)";

    @Override
    public void contributeFunctions(final FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
            .registerPattern(FUNCTION2_NAME, FUNCTION2_PATTERN,
                functionContributions.getTypeConfiguration().getBasicTypeRegistry()
                    .resolve(DOUBLE));
    }
}
```

위와같이 `FunctionContributor`를 구현하고,

`resources/META-INF/services/org.hibernate.boot.model.FunctionContributor` 파일을 생성하여 해당 FunctionContributor 경로를 등록해준다.

<img width="800" src="https://github.com/user-attachments/assets/b88544f6-a8d5-4140-b646-644816e364e9" />

마지막으로 QueryDSL을 사용하는 Repository에서 아래와 같이 커스텀 함수를 사용해주었다.

```java
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Override
    public List<Menu> searchByKeyword(String keyword) {
        QMenu qMenu = QMenu.menu;
        QShop qShop = QShop.shop;

        return queryFactory.selectFrom(qMenu)
            .join(qMenu.shop, qShop).fetchJoin()
            .where(qMenu.isDeleted.isFalse()
                .and(createSearchCondition(qMenu, keyword)))
            .fetch();
    }

    private BooleanExpression createSearchCondition(QMenu qMenu, String keyword) {
        if (isMySQL()) {
            // MySQL 에서는 사용자 정의 함수 사용
            NumberExpression<Double> searchCondition = Expressions.numberTemplate(Double.class,
                "match_2params_against({0}, {1}, {2})", //커스텀 함수 사용
                qMenu.name, qMenu.description, keyword
            );
            return searchCondition.gt(0.0);
        } else {
            // 다른 DB 에서는 일반적인 문자열 포함 검사 사용
            return qMenu.description.containsIgnoreCase(keyword)
                .or(qMenu.name.containsIgnoreCase(keyword));
        }
    }

    private boolean isMySQL() {
        return driverClassName != null && driverClassName.contains("mysql");
    }
}
```

이 쿼리문은 MySQL에서만 동작하기 때문에, MySQL이 아닐 때에는 일반 Like문으로 돌아가도록 했다.

<br/>

### 최종 결과

이 변경사항을 Postman을 통해 다시 테스트해보았다.

<div align="center"><img width="503" alt="스크린샷 2025-01-10 오후 4 25 18" src="https://github.com/user-attachments/assets/ae68c239-3fb0-4e60-86dd-b2d744c6394a" /></div>

실행 결과를 확인해보면 평균 134ms로, `1.6초` -> `0.134초` 약 12배 가까이 빨라졌다.
