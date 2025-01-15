package yeim.aop.app.v4;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yeim.aop.trace.logtrace.LogTrace;
import yeim.aop.trace.template.AbstractTemplate;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV4 {

	private final LogTrace trace;

	public void save(String memberId) {
		AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {
			@Override
			protected Void call() {
				//저장 로직
				if (memberId.equals("ex")) {
					throw new IllegalStateException("예외 발생!");
				}
				sleep(1000);
				return null;
			}
		};
		template.execute("MemberRepository.save()");
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
