package dynamic2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DynamicMethodCaller {
    private final static Controller controller = new Controller();
    private final static Map<String, Method> methodMap = new HashMap<>();

    private static void initializeMapping() {
        System.out.println("==사용가능 url==");
        for (Method method : Controller.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandMapping.class)) {
                String url = method.getAnnotation(CommandMapping.class).value();
                methodMap.put(url, method);
                System.out.println(url);
            }
        }
    }

    private static void executeMethod(String methodName) throws ReflectiveOperationException {
        if (methodMap.containsKey(methodName)) {
            Method method = methodMap.get(methodName);
            String result = (String) method.invoke(controller);
            System.out.printf("==메서드 반환값==\n%s\n\n", result);
        } else {
            System.out.println("그 메서드는 찾을 수 없어");
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            initializeMapping();
            while (true) {
                System.out.print("호출하고 싶은 메서드를 입력하세요 (종료는 exit): ");
                String methodName = scanner.nextLine();
                if ("exit".equalsIgnoreCase(methodName)) {
                    break;
                }
                executeMethod(methodName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
