package valid;

import valid.domain.MemberCreate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MemberValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberCreate.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberCreate memberCreate = (MemberCreate) target;

        // 필드 검증
        if (!StringUtils.hasText(memberCreate.getName())
            || !memberCreate.getName().matches("^[a-z][a-zA-Z0-9]{3,13}$")) {
            errors.rejectValue("name", "name.error",
                "영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자");
        }
        if (!StringUtils.hasText(memberCreate.getPassword())
            || !memberCreate.getPassword().matches("^[A-Z][a-zA-Z0-9]{3,13}$")) {
            errors.rejectValue("password", "password.error",
                "영어와 숫자로만, 첫 글자는 대문자, 최소 4자, 최대 14자");
        }
        if (memberCreate.getMoney() == null
            || memberCreate.getMoney() < 1_000
            || memberCreate.getMoney() > 1_000_000) {
            errors.rejectValue("money", "money.error",
                "금액은 1,000원 이상 1,000,000원 이하로만 가능합니다.");
        }

        //복합 검증
        if (memberCreate.getName().startsWith("admin") &&
            !memberCreate.getPassword().startsWith("admin")) {
            errors.reject("member.combination.error", "사용할 수 없는 아이디와 비밀번호 조합입니다.");
        }
    }
}
