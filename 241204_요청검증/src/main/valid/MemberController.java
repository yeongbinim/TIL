package valid;

import jakarta.annotation.PostConstruct;
import valid.domain.Member;
import valid.domain.MemberCreate;
import valid.domain.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;

    @Autowired
    public MemberController(MemberRepository memberRepository, MemberValidator memberValidator) {
        this.memberRepository = memberRepository;
        this.memberValidator = memberValidator;
    }

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(memberValidator);
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
    public String memberForm(Model model) {
        model.addAttribute("member", new MemberCreate(null, null, null));
        return "member/new-form";
    }

    @PostMapping("/add")
    public String createMember(@Validated @ModelAttribute("member") MemberCreate memberCreate,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/new-form";
        }

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
