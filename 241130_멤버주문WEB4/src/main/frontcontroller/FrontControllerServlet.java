package frontcontroller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "frontControllerServlet", urlPatterns = "/front-controller/*")
public class FrontControllerServlet extends HttpServlet {
    private Map<String, Controller> controllerMap = new HashMap<>();

    public FrontControllerServlet() {
        controllerMap.put("/front-controller/members/new-form", new MemberFormController());
        controllerMap.put("/front-controller/members/save", new MemberSaveController());
        controllerMap.put("/front-controller/members", new MemberListController());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        Controller controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        MyView view = controller.process(request, response);
        view.render(request, response);
    }
}