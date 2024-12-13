package frontcontroller;

import java.util.Map;

public interface Controller {
    ModelView process(Map<String, String> paramMap);
}