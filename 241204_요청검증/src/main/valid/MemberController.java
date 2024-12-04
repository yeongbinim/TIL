package mvc;

import jakarta.annotation.PostConstruct;
import mvc.domain.Member;
import mvc.domain.MemberCreate;
import mvc.domain.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String createMember(@ModelAttribute("member") MemberCreate memberCreate,
        RedirectAttributes redirectAttributes) {
        Member member = memberRepository.save(Member.from(memberCreate));
        redirectAttributes.addAttribute("id", member.getId());
        return "redirect:/members/{id}";
    }

    @PostConstruct
    public void init() {
        memberRepository.save(new Member(null, "yeim", "1234", 10000));
        memberRepository.save(new Member(null, "hgo", "1234", 10000));
    }
}
