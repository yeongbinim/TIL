package yeim.infrastructure;

import static lombok.AccessLevel.PRIVATE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import yeim.domain.Member;

@Entity
@Table(name = "member")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class MemberEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public static MemberEntity from(Member member) {
		return new MemberEntity(
			member.getId(),
			member.getName(),
			member.getEmail(),
			member.getPassword(),
			member.getCreatedAt(),
			member.getUpdatedAt()
		);
	}

	public Member toModel() {
		return new Member(
			id,
			name,
			email,
			password,
			createdAt,
			updatedAt
		);
	}
}
