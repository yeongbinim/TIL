package yeim.aop.app.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberControllerV0 {

	private final MemberServiceV0 memberService;

	@GetMapping("/v0/request")
	public String request(String memberId) {
		memberService.createMember(memberId);
		return "ok";
	}
}
