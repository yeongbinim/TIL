# N + 1 문제

```java
public class JdbcTemplateScheduleRepository implements ScheduleRepository {

	@Override
	public PageResponse<Schedule> findAll(int page, int size) {
		String rowCountSql = "SELECT count(*) FROM schedule";
		int total = template.queryForObject(rowCountSql, Integer.class);

		int offset = page * size;
		String sql = """
			SELECT id, member_id, content, created_at, updated_at
			FROM schedule
			ORDER BY updated_at DESC
			LIMIT ? OFFSET ?
			""";
		List<Schedule> schedules = template.query(
			sql,
			new Object[]{size, offset},
			scheduleRowMapper()
		);

		return new PageResponse<>(schedules, page, size, total);
	}

	private RowMapper<Schedule> scheduleRowMapper() {
		return (rs, rowNum) -> {
			//여기 집중
			Member member = memberRepository.findById(rs.getLong("member_id")).orElse(null);
			return new Schedule(
				rs.getLong("id"),
				member,
				rs.getString("content"),
				rs.getTimestamp("created_at").toLocalDateTime(),
				rs.getTimestamp("updated_at").toLocalDateTime()
			);
		};
	}
}
```

위는 Schedule 테이블로부터 모든 데이터를 가져오는 함수와, 이걸 Schedule 객체로 매핑하는 함수이다.

Schedule은 Member를 바라보고 있기 때문에, 해당 멤버들을 연결시켜주기 위해서 n개의 Schedule 데이터를 불러오면 n번의 쿼리를 추가로 날리고 있는 셈이었다.

실제로 debug 모드를 켜고 확인해보니 아래와 같은 쿼리가 계속 날라갔다.

<img width="150" src="https://github.com/user-attachments/assets/bb6d6386-b246-4594-9f0d-882de151e468">

저 member를 캐싱하는 방법도 있겠지만, 가장 간단한 방법으로 JOIN문을 통해서 해결해보았다.

```java
public Optional<Schedule> findById(Long id) {
	String sql = """
		SELECT s.id, s.member_id, s.content, s.created_at, s.updated_at,
		       m.name as member_name, m.email as member_email, m.password as member_password, m.created_at as member_created_at, m.updated_at as member_updated_at
		FROM schedule s
		JOIN member m ON s.member_id = m.id
		WHERE s.id = ?
		""";
	Schedule schedule = template.queryForObject(sql, scheduleRowMapper(), id);
	return Optional.ofNullable(schedule);
}
```

1개의 쿼리만 날아가는 것이 확인 가능하다.

<img width="1000" src="https://github.com/user-attachments/assets/b640d25c-fbca-4bd0-800b-c40ca574180c">

JPA는 어떻게 이 문제를 해결할지 궁금해졌다. 추후에 좀 더 학습해보자

내가 봤던 n+1문제는 1:n관계에서 1을 불러올때 그와 연관된 n을 n번의 쿼리로 불러오는
거였는데, 이렇게 n을 불러올때 1을 n번의 쿼리로 불러오는 것도 n+1 문제일 수도 있겠구나 생각이 들었다. 
