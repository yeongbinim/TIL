package yeim.infrastructure;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import yeim.domain.Member;

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
		return jpaMemberRepository.findAll().stream().map(MemberEntity::toModel).toList();
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
