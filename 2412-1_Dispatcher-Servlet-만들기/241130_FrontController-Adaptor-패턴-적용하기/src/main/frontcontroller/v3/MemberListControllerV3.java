package frontcontroller;

import domain.Member;
import domain.MemberRepository;

import java.util.List;
import java.util.Map;

public class MemberListControllerV3 implements Controller {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        List<Member> members = memberRepository.findAll();
        ModelView mv = new ModelView("members");
        mv.getModel().put("members", members);
        return mv;
    }
}