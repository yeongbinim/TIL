package frontcontroller;

import domain.Member;
import domain.MemberRepository;

import java.util.Map;

public class MemberSaveControllerV4 implements Controller {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        String username = paramMap.get("username");
        String password = paramMap.get("password");
        Member member = new Member(null, username, password);
        memberRepository.save(member);
        model.put("member", member);
        return "save-result";
    }
}