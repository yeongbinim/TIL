package yeim.aop.app.v3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.logtrace.LogTrace;

@Service
@RequiredArgsConstructor
public class MemberServiceV3 {
	private final MemberRepositoryV3 memberRepository;
	private final LogTrace trace;

	public void createMember(String memberId) {
		TraceStatus status = trace.begin("MemberService.createMember()");
		try {
			memberRepository.save(memberId);
			trace.end(status);
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}
}
