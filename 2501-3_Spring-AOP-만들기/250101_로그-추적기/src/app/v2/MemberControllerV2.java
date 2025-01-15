package yeim.aop.app.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yeim.aop.trace.TraceStatus;
import yeim.aop.trace.hellotrace.HelloTraceV2;

@RestController
@RequiredArgsConstructor
public class MemberControllerV2 {

	private final MemberServiceV2 memberService;
	private final HelloTraceV2 trace;

	@GetMapping("/v2/request")
	public String request(String memberId) {
		TraceStatus status = trace.begin("MemberController.request()");
		try {
			memberService.createMember(status.getTraceId(), memberId);
			trace.end(status);
			return "ok";
		} catch (Exception e) {
			trace.exception(status, e);
			throw e; //예외를 꼭 다시 던져주어야 한다.
		}
	}
}
