package dynamic3;

import dynamic3.annotation.CommandMapping;
import dynamic3.annotation.Controller;

@Controller
@CommandMapping("/banana")
public class BananaController {
    @CommandMapping("/create")
    public String create() {
        return "Banana create 의 결과값";
    }

    @CommandMapping("/login")
    public String login() {
        return "Banana login 의 결과값";
    }
}
