package config;

import config.annotation.CommandMapping;
import config.annotation.Controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMappingHandler {
    private final ApplicationContext context;
    private final ComponentScanner scanner = ComponentScanner.getInstance();
    private final Map<String, Method> methodMap = new HashMap<>();

    public CommandMappingHandler(ApplicationContext context) {
        this.context = context;
        initializeMapping();
    }

    private void initializeMapping()  {
        List<Class<?>> classList = scanner.componentScan(Controller.class);
        System.out.println("==사용가능 url==");
        for (Class<?> clazz : classList) {
            String basePath = "/";
            if (clazz.isAnnotationPresent(CommandMapping.class)) {
                basePath = clazz.getAnnotation(CommandMapping.class).value();
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CommandMapping.class)) {
                    String fullPath = basePath + method.getAnnotation(CommandMapping.class).value();
                    String httpMethod = method.getAnnotation(CommandMapping.class).method();
                    methodMap.put(httpMethod + " " + fullPath, method);
                    System.out.println(httpMethod + " " + fullPath);
                }
            }
        }
    }

    public void execute(String command) throws ReflectiveOperationException {
        if (methodMap.containsKey(command)) {
            Method method = methodMap.get(command);
            Object instance = context.getBean(method.getDeclaringClass());
            String result = (String) method.invoke(instance);
            System.out.printf("==메서드 반환값==\n%s\n\n", result);
        } else {
            System.out.println("그 메서드는 찾을 수 없어");
        }
    }
}
