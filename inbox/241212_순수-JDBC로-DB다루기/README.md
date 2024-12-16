# 순수 JDBC 다루기

JPA 를 사용하기 앞서 순수 JDBC를 다뤄보려 한다.

JDBC 템플릿이라는 SQL Mapper를 사용하지 않고 직접 해보면서 문제를 분석해볼 것이다.

<br>

### 목차

- [DriveManager로 Connection 불러오기](#drivemanager로-connection-불러오기)
- [DataSource로 Connection 불러오기](#datasource로-connection-불러오기)
- [Statement으로 Create, Update, Delete 하기](#statement으로-create-update-delete-하기)
- [ResultSet 불러오기](#resultset-불러오기)
- [마치며](#마치며)

<br/>

### DriveManager로 Connection 불러오기

우선 JDBC 라는 기술은 커넥션 연결, SQL 전달 및 응답 처리 방법이 각 DB마다 다르다는 맥락에서 나온 표준이다.

MySQL, PostgreSql... 여러가지 DB들이 있겠지만, 각 DB 드라이버들은 이 JDBC라는 표준의 3가지 인터페이스를 구현하고 있다. (`Connection`, `Statement`, `ResultSet`)

실제 드라이버로부터 Connection을 불러오기 위해 아래와 같이 작성했다.

```java
private Connection getConnection() throws SQLException {
  Connection connection = DriverManager.getConnection(URL, USERNANE, PASSWORD);
  return connection;
}
```

<div align="center"><img width="400" alt="스크린샷 2024-12-16 오후 10 37 30" src="https://github.com/user-attachments/assets/25b34b2a-31d8-49e1-baf6-a96c1205a323" /></div>

`DriverManager.getConnection(연결정보)`을 통해 등록된 드라이버 목록에서 일일이 커넥션 획득 가능한지 확인하여 실제 드라이버의 Connection을 얻어오는 것이다.

<br/>

### DataSource로 Connection 불러오기

하지만 매번 요청마다 커넥션을 새로 만드는 것은 리소스 소모가 크다. 

따라서 **커넥션 풀에 미리 확보**해 두는 방법을 주로 사용하는데, 스프링 부트는 기본으로 hikariCP를 사용한다.

그렇다고 hikariCP에 다시 직접적으로 의존할 수 없기 때문에, 이번에는 **DataSource**를 활용한다.

```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {
  private final DataSource dataSource;

  private Connection getConnection() throws SQLException {
    Connection con = dataSource.getConnection();
    return con;
  }
}
```

<div align="center"><img width="400" alt="스크린샷 2024-12-16 오후 10 38 16" src="https://github.com/user-attachments/assets/d75f713a-14d3-4b84-8ff7-5d6648d4096f" /></div>

이전과 달라진 점은 DriverManager는 `.getConnection(연결정보)` 마다 연결정보를 넘겼지만, DataSource는 인스턴스 생성할 때 한번만 넘겨서 connection을 받아온다는 점이다.

<br/>

### Statement으로 Create, Update, Delete 하기

PreparedStatement의 `.executeUpdate()` 메서드를 통해 Create, Update, Delete가 가능하다. 그 중 create부분만 확인해보자

```java
public class MemberRepository {
  public Member save(Member member) throws SQLException {
    String sql = "INSERT INTO member(member_id, money) values(?, ?)";
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = getConnection();
      pstmt = con.prepareStatement(sql);
      pstmt.setString(1, member.getMemberId());
      pstmt.setInt(2, member.getMoney());
      pstmt.executeUpdate();
      return member;
    } catch (SQLException e) {
      log.error("db error", e);
      throw e;
    } finally {
      close(con, pstmt, null);
    }
  }

  private void close(Connection con, Statement stmt, ResultSet rs) {
    JdbcUtils.closeResultSet(rs);
    JdbcUtils.closeStatement(stmt);
    JdbcUtils.closeConnection(con);
  }
}
```

실제 sql 쿼리문과, connection으로부터 얻어온 statement에 `.setString()` 이걸로 파라미터 정보를 넘기고 (? 로 하는 이유는 SqlInjection 방지를 위해서다) 마지막으로 `executeUpdate()` 을 호출한다.

con, stmt, rs 모두 닫아줘야 하는데, 이것도... null 처리 하면 귀찮으므로 JdbcUtils 에서 제공하는 함수를 사용했다.

<br/>

### ResultSet 불러오기

SELECT문의 경우에는 `executeUpdate()` 대신 `executeQuery()` 를 통해 결과 값들을 ResultSet 타입으로 받아오는데 이때, rs.next()를 통해 불러온 결과에서 한 행씩 이동시키는 메커니즘이다.

<img width="402" alt="스크린샷 2024-12-16 오후 10 57 42" src="https://github.com/user-attachments/assets/96c7e773-7335-4b34-90bf-1d225eeb85ae" />

구현 코드를 살펴보면 아래와 같다.

```java
public class MemberRepository {
  public Member findById(String memberId) throws SQLException {
    String sql = "select * from member where member_id = ?";
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      con = getConnection();
      pstmt = con.prepareStatement(sql);
      pstmt.setString(1, memberId);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        String id = rs.getString("member_id");
        int money = rs.getInt("money");
        return new Member(id, money);
      } else {
        throw new NoSuchElementException("member not found memberId=" + memberId);
      }
    } catch (SQLException e) {
      log.error("db error", e);
      throw e;
    } finally {
      close(con, pstmt, rs);
    }
  }
}
```

우선 하나만 찾는다는 가정하에, 처음에 rs.next()를 호출해서 커서를 한 번은 이동시켜줘야 하고, 결과 값을 반환한 이후 마찬가지로 con, pstmt, rs 모두 close 해주었다.

<br/>

### 마치며

JDBC로 개발 흐름을 요약해보면 아래와 같다.

1. `DataSource.getConnection()`을 통해 **커넥션 풀로부터 실제 드라이버의 `Connection`을 얻어온다.**
2. `con.prepareStatement(sql)`를 통해 **`Statement`를 얻어와서** `stmt.setString(1, id)`로 DB에 전달할 파라미터 정보를 세팅한다.
3. `stmt.executeQuery()`를 통해 실제 DB에 전달한 결과를 **`ResultSet`을 얻어와서** `rs.next()`로 **커서(행)를 이동**시키며 값을 `rs.getString("id")`로 속성 값을 조회한다.
4. 마지막으로 **rs, stmt, con 모두 `close()`**하는데, 더 쉽게 close 하기 위해 `JdbcUtils`의 `.closeResultSet(rs)`, `.closeStatement(stmt)`, `.closeConnection(con)` 사용

JDBC로 했을 때 아쉬운 점은... 저 SQLException 거슬린다는 거...? 그리고 try catch 너무 지겹긴 하다.. 너무 반복되는 느낌... close도 그렇고..

다음으로 JDBC Template이 어떤걸 개선해주는지 알아보자