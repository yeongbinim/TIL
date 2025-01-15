package yeim.aop.app.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yeim.aop.trace.TraceId;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.hellotrace.HelloTraceV2;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV2 {

	private final HelloTraceV2 trace;

	public void save(TraceId traceId, String memberId) {
		TraceStatus status = trace.beginSync(traceId, "MemberRepository.save()");

		try {
			// 저장 로직
			if (memberId.equals("ex")) {
				throw new IllegalArgumentException("예외 발생!");
			}
			sleep(1000);
			trace.end(status);
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
