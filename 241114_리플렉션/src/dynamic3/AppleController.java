package dynamic3;

import dynamic3.annotation.CommandMapping;
import dynamic3.annotation.Controller;

@Controller
@CommandMapping("/apple")
public class AppleController {
    @CommandMapping("/create")
    public String create() {
        return "Apple create 의 결과값";
    }

    @CommandMapping("/login")
    public String login() {
        return "Apple login 의 결과값";
    }
}
