package yeim.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yeim.domain.Member;
import yeim.infrastructure.MemberRepositoryV2;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

	private final DataSource dataSource;
	private final MemberRepositoryV2 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		Connection con = dataSource.getConnection();
		try {
			con.setAutoCommit(false); //트랜잭션 시작
			//비즈니스 로직
			bizLogic(con, fromId, toId, money);
			con.commit();
		} catch (Exception e) {
			con.rollback();
			throw new IllegalStateException(e);
		} finally {
			release(con);
		}
	}


	private void bizLogic(Connection con, String fromId, String toId, int money)
		throws SQLException {
		Member fromMember = memberRepository.findById(con, fromId);
		Member toMember = memberRepository.findById(con, toId);
		memberRepository.update(con, fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepository.update(con, toId, toMember.getMoney() + money);
	}

	private static void validation(Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}

	private void release(Connection con) {
		if (con != null) {
			try {
				con.setAutoCommit(true); //커넥션 풀 고려 con.close();
			} catch (Exception e) {
				log.info("error", e);
			}
		}
	}
}
