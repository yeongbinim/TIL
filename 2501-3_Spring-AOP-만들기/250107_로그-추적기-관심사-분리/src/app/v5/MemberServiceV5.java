package yeim.aop.app.v5;

import org.springframework.stereotype.Service;
import yeim.aop.trace.callback.TraceTemplate;
import yeim.aop.trace.logtrace.LogTrace;

@Service
public class MemberServiceV5 {
	private final MemberRepositoryV5 memberRepository;
	private final TraceTemplate template;

	public MemberServiceV5(MemberRepositoryV5 memberRepository, LogTrace trace) {
		this.memberRepository = memberRepository;
		this.template = new TraceTemplate(trace);
	}

	public void createMember(String memberId) {
		template.execute("MemberService.createMember()", () -> {
			memberRepository.save(memberId);
			return null;
		});
	}
}
