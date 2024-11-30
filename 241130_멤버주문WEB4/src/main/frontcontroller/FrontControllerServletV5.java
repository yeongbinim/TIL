package frontcontroller;

import frontcontroller.adaptor.ControllerV3HandlerAdaptor;
import frontcontroller.adaptor.ControllerV4HandlerAdaptor;
import frontcontroller.adaptor.HandlerAdaptor;
import frontcontroller.v3.MemberFormControllerV3;
import frontcontroller.v3.MemberListControllerV3;
import frontcontroller.v3.MemberSaveControllerV3;
import frontcontroller.v4.MemberFormControllerV4;
import frontcontroller.v4.MemberListControllerV4;
import frontcontroller.v4.MemberSaveControllerV4;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<HandlerAdaptor> handlerAdaptors = new ArrayList<>();

    public FrontControllerServletV5() {
        initHandlerMappingMap();
        initHandlerAdaptors();
    }

    private void initHandlerMappingMap() { //추가
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

        handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
    }

    private void initHandlerAdaptors() { //추가
        handlerAdaptors.add(new ControllerV3HandlerAdaptor());
        handlerAdaptors.add(new ControllerV4HandlerAdaptor());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object handler = getHandler(request);
        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        HandlerAdaptor adaptor = getHandlerAdaptor(handler);
        ModelView mv = adaptor.handle(request, response, handler);
        MyView view = viewResolver(mv.getViewName());
        view.render(mv.getModel(), request, response);
    }

    private Object getHandler(HttpServletRequest request) { //추가
        String requestURI = request.getRequestURI();
        return handlerMappingMap.get(requestURI);
    }

    private HandlerAdaptor getHandlerAdaptor(Object handler) { //추가
        for (HandlerAdaptor adaptor : handlerAdaptors) {
            if (adaptor.supports(handler)) {
                return adaptor;
            }
        }
        throw new IllegalArgumentException("handler adaptor를 찾을 수 없습니다. handler=" + handler);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}