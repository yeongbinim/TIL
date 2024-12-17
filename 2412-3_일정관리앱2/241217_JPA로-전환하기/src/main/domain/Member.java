package yeim.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Member {
	@Setter
	private Long id;
	private String name;
	private String email;
	private String password;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static Member from(MemberCreate memberCreate) {
		return new Member(
			null,
			memberCreate.getName(),
			memberCreate.getEmail(),
			memberCreate.getPassword(),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
	}

	public Member update(MemberUpdate memberUpdate) {
		return new Member(
			id,
			memberUpdate.getName(),
			memberUpdate.getEmail(),
			password,
			createdAt,
			LocalDateTime.now()
		);
	}

	public boolean verifyPassword(String password) {
		return this.password.equals(password);
	}
}
