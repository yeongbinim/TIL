package yeim.aop.app.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceV0 {
	private final MemberRepositoryV0 memberRepository;

	public void createMember(String memberId) {
		memberRepository.save(memberId);
	}
}
