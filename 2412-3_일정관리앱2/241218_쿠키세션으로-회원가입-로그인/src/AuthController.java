package yeim.jpa_scheduler.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<Void> register(
		@Valid @RequestBody MemberCreate memberCreate,
		HttpServletRequest req
	) {
		Member member = authService.createMember(memberCreate);
		HttpSession session = req.getSession(true);
		session.setAttribute("memberId", member.getId());
		return ResponseEntity
			.created(URI.create("/members/" + member.getId()))
			.build();
	}

	@PostMapping("/login")
	public ResponseEntity<Void> login(
		@Valid @RequestBody MemberLogin memberLogin,
		HttpServletRequest req
	) {
		Member member = authService.validateMember(memberLogin);
		HttpSession session = req.getSession(true);
		session.setAttribute("memberId", member.getId());

		return ResponseEntity
			.ok()
			.build();
	}
}
