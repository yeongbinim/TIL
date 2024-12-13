package member.service;

import config.annotation.Autowired;
import config.annotation.Component;
import member.infrastructure.MemberRepository;
import member.model.Member;
import member.model.MemberCreate;

import java.util.List;
import java.util.NoSuchElementException;

@Component
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member join(MemberCreate memberCreate) {
        return memberRepository.save(Member.from(memberCreate));
    }

    @Override
    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No member found with id " + id));
    }

    @Override
    public List<Member> getAll() {
        return memberRepository.findAll();
    }
}