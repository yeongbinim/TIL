package yeim.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import yeim.domain.Member;

@Repository
public class MemoryMemberRepository implements MemberRepository {

	private final Map<Long, Member> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong();

	@Override
	public Member create(Member member) {
		member.setId(sequence.incrementAndGet());
		store.put(member.getId(), member);

		return member;
	}

	@Override
	public List<Member> findAll() {
		return new ArrayList<>(store.values());
	}

	@Override
	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public Member update(Member member) {
		member.setId(member.getId());
		store.put(member.getId(), member);
		return member;
	}

	@Override
	public void delete(Long id) {
		store.remove(id);
	}
}
