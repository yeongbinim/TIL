package yeim.aop.app.v5;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yeim.aop.trace.callback.TraceTemplate;
import yeim.aop.trace.logtrace.LogTrace;

@RestController
public class MemberControllerV5 {

	private final MemberServiceV5 memberService;
	private final TraceTemplate template;

	public MemberControllerV5(MemberServiceV5 memberService, LogTrace trace) {
		this.memberService = memberService;
		this.template = new TraceTemplate(trace);
	}

	@GetMapping("/v5/request")
	public String request(String memberId) {
		return template.execute("MemberController.request()", () -> {
			memberService.createMember(memberId);
			return "ok";
		});
	}
}
