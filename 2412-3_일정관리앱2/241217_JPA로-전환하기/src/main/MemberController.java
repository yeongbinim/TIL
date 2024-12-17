package yeim;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yeim.domain.Member;
import yeim.domain.MemberCreate;
import yeim.domain.MemberDelete;
import yeim.domain.MemberResponse;
import yeim.domain.MemberUpdate;
import yeim.infrastructure.MemberRepository;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	@Autowired
	private MemberRepository memberRepository;

	@PostMapping
	public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreate memberCreate) {
		Member member = memberRepository.create(Member.from(memberCreate));
		return ResponseEntity
			.created(URI.create("/members/" + member.getId()))
			.build();
	}

	@GetMapping
	public ResponseEntity<List<MemberResponse>> getAllMembers() {
		List<MemberResponse> members = memberRepository.findAll().stream()
			.map(MemberResponse::from)
			.toList();

		return ResponseEntity
			.ok()
			.body(members);
	}

	@GetMapping("/{id}")
	public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("찾을 수 없다."));

		return ResponseEntity
			.ok()
			.body(MemberResponse.from(member));
	}

	@PutMapping("/{id}")
	public ResponseEntity<MemberResponse> updateMember(
		@PathVariable Long id,
		@Valid @RequestBody MemberUpdate memberUpdate
	) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("찾을 수 없다."));
		if (!member.verifyPassword(memberUpdate.getPassword())) {
			throw new IllegalArgumentException("비밀번호 잘못됨");
		}
		member = memberRepository.update(member.update(memberUpdate));
		return ResponseEntity
			.ok()
			.body(MemberResponse.from(member));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<MemberResponse> deleteMember(
		@PathVariable Long id,
		@Valid @RequestBody MemberDelete memberDelete
	) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("찾을 수 없다."));
		if (!member.verifyPassword(memberDelete.getPassword())) {
			throw new IllegalArgumentException("비밀번호 잘못됨");
		}
		memberRepository.delete(id);
		return ResponseEntity
			.noContent()
			.build();
	}
}
