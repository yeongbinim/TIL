package yeim.aop.app.v4;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.logtrace.LogTrace;
import yeim.aop.trace.template.AbstractTemplate;

@Service
@RequiredArgsConstructor
public class MemberServiceV4 {
	private final MemberRepositoryV4 memberRepository;
	private final LogTrace trace;

	public void createMember(String memberId) {
		AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {
			@Override
			protected Void call() {
				memberRepository.save(memberId);
				return null;
			}
		};
		template.execute("MemberService.createMember()");
	}
}
