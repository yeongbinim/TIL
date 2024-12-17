package yeim.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberUpdate {
	@NotEmpty
	private String name;
	@NotEmpty
	private String email;
	@NotEmpty
	private String password;

}
