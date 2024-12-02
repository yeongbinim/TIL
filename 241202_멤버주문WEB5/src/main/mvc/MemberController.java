package mvc;

import jakarta.annotation.PostConstruct;
import mvc.domain.Member;
import mvc.domain.MemberCreate;
import mvc.domain.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
public class MemberController {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public String members(Model model) {
        model.addAttribute("members", memberRepository.findAll());
        return "member/members";
    }

    @GetMapping("/{memberId}")
    public String member(@PathVariable Long memberId, Model model) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return "redirect:/add";
        }
        model.addAttribute("member", member);
        return "member/save-result";
    }

    @GetMapping("/add")
    public String memberForm() {
        return "member/new-form";
    }

    @PostMapping("/add")
    public String createMember(@ModelAttribute("member") MemberCreate memberCreate) {
        Member member = memberRepository.save(Member.from(memberCreate));
        return "redirect:/members/" + member.getId();
    }

    @PostConstruct
    public void init() {
        memberRepository.save(new Member(null, "yeim", "1234"));
        memberRepository.save(new Member(null, "hgo", "1234"));
    }
}
