package yeim.aop.app.v3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yeim.aop.trace.TraceId;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.hellotrace.HelloTraceV2;
import yeim.aop.trace.logtrace.LogTrace;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV3 {

	private final LogTrace trace;

	public void save(String memberId) {
		TraceStatus status = trace.begin("MemberRepository.save()");

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
