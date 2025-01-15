package yeim.aop.app.v4;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yeim.aop.trace.logtrace.LogTrace;
import yeim.aop.trace.template.AbstractTemplate;

@RestController
@RequiredArgsConstructor
public class MemberControllerV4 {

	private final MemberServiceV4 memberService;
	private final LogTrace trace;

	@GetMapping("/v4/request")
	public String request(String memberId) {
		AbstractTemplate<String> template = new AbstractTemplate<>(trace) {
			@Override
			protected String call() {
				memberService.createMember(memberId);
				return "ok";
			}
		};
		return template.execute("MemberController.request()");
	}
}
