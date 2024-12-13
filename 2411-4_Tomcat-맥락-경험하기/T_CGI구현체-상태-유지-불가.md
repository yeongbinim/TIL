# CGI구현체 상태 유지 불가

처음에는 MemberRepository를 아래처럼 작성했었다.

```java
public class MemberRepository {
    private static final Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;

    public static MemberRepository getInstance() {
        return new MemberRepository();
    }

    private MemberRepository() {}

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
}
```

하지만 회원을 아무리 생성해도 회원이 조회가 안되는 놀라운 일이 생겼다.

그 이유는 저 MemberRepository 자체가 메모리에 계속 남아있는게 아니라 해제되고 새로운 요청이 오면 또 새로 만들어지고... 그러기 때문에..

왜 안되는거야!!! 하면서 이것도 10분 넘게 헤매긴했다 ㅋㅋㅋㅋ 지금 생각해보면 참 바보....

따라서 CSV에 저장하고 조회하는 코드로 바꿨다. 궁금하면 코드를 살펴보자.
