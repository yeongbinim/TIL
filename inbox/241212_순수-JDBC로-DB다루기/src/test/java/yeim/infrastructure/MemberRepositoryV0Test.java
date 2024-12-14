package yeim.infrastructure;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import yeim.domain.Member;

class MemberRepositoryV0Test {

	MemberRepositoryV0 repository = new MemberRepositoryV0();

	@Test
	void crud() throws SQLException {
		Member member = new Member("memberV0", 10000);
		repository.save(member);
		System.out.println(repository.findById("memberV0").getMemberId());
	}
}
