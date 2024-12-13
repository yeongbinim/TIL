package frontcontroller;

import java.util.Map;

public class MemberFormControllerV4 implements Controller {
    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        return "new-form";
    }
}