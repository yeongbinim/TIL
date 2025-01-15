package yeim.aop.app.v5;

import org.springframework.stereotype.Repository;
import yeim.aop.trace.callback.TraceTemplate;
import yeim.aop.trace.logtrace.LogTrace;

@Repository
public class MemberRepositoryV5 {

	private final TraceTemplate template;

	public MemberRepositoryV5(LogTrace trace) {
		this.template = new TraceTemplate(trace);
	}

	public void save(String memberId) {
		template.execute("MemberRepository.save()", () -> {
			//저장 로직
			if (memberId.equals("ex")) {
				throw new IllegalStateException("예외 발생!");
			}
			sleep(1000);
			return null;
		});
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
