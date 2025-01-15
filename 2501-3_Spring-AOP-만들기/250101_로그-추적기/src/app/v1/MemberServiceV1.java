package yeim.aop.app.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.hellotrace.HelloTraceV1;

@Service
@RequiredArgsConstructor
public class MemberServiceV1 {
	private final MemberRepositoryV1 memberRepository;
	private final HelloTraceV1 trace;

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
