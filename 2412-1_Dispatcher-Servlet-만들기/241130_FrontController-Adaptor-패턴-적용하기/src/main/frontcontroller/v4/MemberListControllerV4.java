package frontcontroller;

import domain.Member;
import domain.MemberRepository;

import java.util.List;
import java.util.Map;

public class MemberListControllerV4 implements Controller {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        List<Member> members = memberRepository.findAll();
        model.put("members", members);
        return "members";
    }
}