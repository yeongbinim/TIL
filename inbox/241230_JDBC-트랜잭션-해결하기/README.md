# JDBC 트랜잭션 해결하기

DB가 제공하는 기능 중에서 가장 강력한 기능을 꼽으라면 트랜잭션이다.

쿼리를 실행하고 결과를 반영하려면 `commit`을, 원하지 않으면 `rollback`을 호출하는 방식으로 진행되며, 해당 트랜잭션을 시작한 세션에게만 변경 데이터가 보이고, 트랜잭션이 완료되기 이전에는 다른 세션에게 반영되지 않는다.

기본적으로 자동 커밋 모드로 되어 있기 때문에, `SET AUTOCOMMIT FALSE` 로 모드를 전환해서 트랜잭션을 시작해야 한다.

이거를 `connection.setAutoCommit(false)` 라는 메서드를 통해서 지원을 하는데, 이를 통해서 순수 JDBC에서 트랜잭션을 보장하는 코드를 작성해보려고 한다.



### 트랜잭션을 지원하지 않는 계좌이체

계좌 이체 서비스를 구현하는데, `ex`라는 이름을 가진 멤버에게는 이체를 하지 못하는 상황을 가정해보자

```java
@RequiredArgsConstructor
public class MemberServiceV1 {

	private final MemberRepositoryV1 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepository.findById(fromId);
		Member toMember = memberRepository.findById(toId);

		memberRepository.update(fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepository.update(toId, toMember.getMoney() + money);
	}

	private static void validation(Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}
}
```

`new IllegalStateException`이 발생해서 그 아래 코드가 실행되지 못해서 fromMember의 Money만 깎이게 된다.

이처럼 비즈니스 로직이 잘못되면 함께 롤백해야 하기 때문에 트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작하고 종료될 수 밖에 없어보인다.



### 트랜잭션을 위한 계좌이체 서비스

그렇다면 서비스 계층에서 트랜잭션을 시작하고 커밋하거나 롤백하는 코드로 변경해보자.

리포지토리 계층에서 각 메서드들에 connection 객체를 넘길 수 있도록 메서드들을 약간 수정하고, 코드를 아래와 같이 작성했다.

```java
@RequiredArgsConstructor
public class MemberServiceV2 {

	private final DataSource dataSource;
	private final MemberRepositoryV2 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		Connection con = dataSource.getConnection();
		try {
			con.setAutoCommit(false); //트랜잭션 시작
			//비즈니스 로직
      Member fromMember = memberRepository.findById(con, fromId);
      Member toMember = memberRepository.findById(con, toId);
      memberRepository.update(con, fromId, fromMember.getMoney() - money);
      validation(toMember);
      memberRepository.update(con, toId, toMember.getMoney() + money);

      con.commit(); //트랜잭션 커밋
		} catch (Exception e) {
			con.rollback(); //트랜잭션 롤백
			throw new IllegalStateException(e);
		} finally {
			release(con);
		}
	}

	private static void validation(Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}

	private void release(Connection con) {
		if (con != null) {
			try {
				con.setAutoCommit(true); // 커넥션 풀 고려해서 close하기 전에 autoCommit true로 변경
        con.close();
			} catch (Exception e) {
				log.info("error", e);
			}
		}
	}
}
```

주의할 점은 connection.close()를 호출한다고 해서 커넥션이 정말로 해제되는것이 아니라, 커넥션 풀로 돌아가기 때문에 close하기 전에 autocommit 모드를 true로 변경해주어야 한다.

정상적으로 예외발생시 fromMember의 계좌에도 돈이 빠져나가지 않았지만, 여기까지 봤을때 아쉬운 점은 현재 순수해야만 하는 이 서비스 계층에서 JDBC 기술에 너무 과하게 의존하고 있다는 점이다.

<br/>

다음 실습에서는 이 서비스계층의 순수함을 잃지 않도록 개선해볼 예정이다.

