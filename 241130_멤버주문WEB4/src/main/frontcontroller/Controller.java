package frontcontroller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Controller {
    MyView process(HttpServletRequest request, HttpServletResponse response);
}