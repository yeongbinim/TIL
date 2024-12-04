import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import valid.domain.MemberCreate;
import org.junit.jupiter.api.Test;

public class BeanValidationTest {

    @Test
    void beanValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        MemberCreate memberCreate = new MemberCreate("hello", "a1232131", 100);
        Set<ConstraintViolation<MemberCreate>> violations = validator.validate(memberCreate);
        for (ConstraintViolation<MemberCreate> violation : violations) {
            System.out.println(violation.getMessage());
        }
    }
}
