package yeim.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Member {

	private final String memberId;
	private final Integer money;
}
