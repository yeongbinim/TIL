package member.controller;

import config.annotation.Autowired;
import config.annotation.CommandMapping;
import config.annotation.Component;
import config.annotation.Controller;
import config.console.ConsoleFormat;
import config.console.ConsoleInput;
import member.model.Member;
import member.model.MemberCreate;
import member.model.MemberGrade;
import member.service.MemberService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Component
@CommandMapping("/members")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @CommandMapping(method = "POST")
    public String create() {
        System.out.println("\n==회원 가입 정보==");
        String name = ConsoleInput.input(
                "사용자 이름을 입력하세요 (영문자로 시작, 4~10자): ",
                "잘못된 형식입니다. 다시 입력하세요: ",
                "^[a-zA-Z][a-zA-Z0-9]{3,9}$");
        MemberGrade grade = MemberGrade.valueOf(ConsoleInput.input(
                "등급 (VIP 또는 NORMAL): ",
                "VIP 또는 NORMAL 로 입력해주세요: ",
                "VIP|NORMAL"));
        String email = ConsoleInput.input(
                "이메일: ",
                "이메일 형식이 올바르지 않습니다: ",
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        String password = ConsoleInput.input(
                "비밀번호: ",
                "영문자로 시작되어야 하며 특수문자는 @$!%*?&만 가능합니다: ",
                "^[A-Za-z][A-Za-z0-9@$!%*?&]{7,}$");
        MemberCreate memberCreate = new MemberCreate(name, email, grade, password);
        Member member = memberService.join(memberCreate);
        return ConsoleFormat.toJson(MemberResponse.from(member));
    }

    @CommandMapping(method = "GET")
    public String getAll() {
        List<Member> retrieveMember = memberService.getAll();
        return ConsoleFormat.toJson(retrieveMember.stream().map(MemberResponse::from).collect(Collectors.toList()));
    }
}
