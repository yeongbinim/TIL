package yeim.aop.app.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV0 {
	public void save(String memberId) {
		// 저장 로직
		if (memberId.equals("ex")) {
			throw new IllegalArgumentException("예외 발생!");
		}
		sleep(1000);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
