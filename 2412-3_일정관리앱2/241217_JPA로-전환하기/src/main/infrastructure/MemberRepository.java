package yeim.infrastructure;

import java.util.List;
import java.util.Optional;
import yeim.domain.Member;

public interface MemberRepository {

	Member create(Member member);

	List<Member> findAll();

	Optional<Member> findById(Long id);

	Member update(Member member);

	void delete(Long id);
}
