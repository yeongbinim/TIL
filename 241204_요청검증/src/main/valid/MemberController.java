package valid;

import jakarta.annotation.PostConstruct;
import valid.domain.Member;
import valid.domain.MemberCreate;
import valid.domain.MemberRepository;
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
    public String memberForm(Model model) {
        model.addAttribute("member", new Member(null, null, null, null));
        return "member/new-form";
    }

    @PostMapping("/add")
    public String createMember(@ModelAttribute("member") MemberCreate memberCreate,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {

        //필드 검증
        if (!StringUtils.hasText(memberCreate.getName())
            || !memberCreate.getName().matches("^[a-z][a-zA-Z0-9]{3,13}$")) {
            bindingResult.addError(
                new FieldError("member", "name", memberCreate.getName(), false, null, null,
                    "영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자"));
        }
        if (!StringUtils.hasText(memberCreate.getPassword())
            || !memberCreate.getPassword().matches("^[A-Z][a-zA-Z0-9]{3,13}$")) {
            bindingResult.addError(
                new FieldError("member", "password", memberCreate.getPassword(), false, null, null,
                    "영어와 숫자로만, 첫 글자는 대문자, 최소 4자, 최대 14자"));
        }
        if (memberCreate.getMoney() == null
            || memberCreate.getMoney() < 1_000
            || memberCreate.getMoney() > 1_000_000) {
            bindingResult.addError(
                new FieldError("member", "money", memberCreate.getMoney(), false, null, null,
                    "금액은 1,000원 이상 1,000,000원 이하로만 가능합니다."));
        }

        //복합 검증
        if (memberCreate.getName().startsWith("admin")
            && !memberCreate.getPassword().startsWith("admin")) {
            bindingResult.addError(
                new ObjectError("member", null, null, "사용할 수 없는 아이디와 비밀번호 조합 입니다."));
        }

        //검증에 실패하면 다시 입력 폼으로
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
