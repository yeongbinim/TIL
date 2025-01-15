package yeim.aop.app.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeim.aop.trace.TraceId;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.hellotrace.HelloTraceV2;

@Service
@RequiredArgsConstructor
public class MemberServiceV2 {
	private final MemberRepositoryV2 memberRepository;
	private final HelloTraceV2 trace;

	public void createMember(TraceId traceId, String memberId) {
		TraceStatus status = trace.beginSync(traceId, "MemberService.createMember()");
		try {
			memberRepository.save(status.getTraceId(), memberId);
			trace.end(status);
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}
}
