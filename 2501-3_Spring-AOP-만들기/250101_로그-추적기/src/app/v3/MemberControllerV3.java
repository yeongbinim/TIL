package yeim.aop.app.v3;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.logtrace.LogTrace;

@RestController
@RequiredArgsConstructor
public class MemberControllerV3 {

	private final MemberServiceV3 memberService;
	private final LogTrace trace;

	@GetMapping("/v3/request")
	public String request(String memberId) {
		TraceStatus status = trace.begin("MemberController.request()");
		try {
			memberService.createMember(memberId);
			trace.end(status);
			return "ok";
		} catch (Exception e) {
			trace.exception(status, e);
			throw e; //예외를 꼭 다시 던져주어야 한다.
		}
	}
}
