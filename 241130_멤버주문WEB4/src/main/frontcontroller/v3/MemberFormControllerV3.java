package frontcontroller;

import java.util.Map;

public class MemberFormControllerV3 implements Controller {
    @Override
    public ModelView process(Map<String, String> paramMap) {
        return new ModelView("new-form");
    }
}