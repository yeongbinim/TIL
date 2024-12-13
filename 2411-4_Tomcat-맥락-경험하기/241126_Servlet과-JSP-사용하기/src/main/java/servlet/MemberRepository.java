package servlet;

import java.util.*;

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