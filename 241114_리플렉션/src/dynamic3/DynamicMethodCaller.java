package dynamic3;

import dynamic3.annotation.CommandMapping;

import java.lang.reflect.Method;
import java.util.*;

public class DynamicMethodCaller {
    private final static Map<String, Method> methodMap = new HashMap<>();
    private final static Map<String, Object> instanceMap = new HashMap<>();

    private static void initializeMapping() throws Exception {
        ComponentScanner componentScan = ComponentScanner.getInstance();
        List<Class<?>> classList = componentScan.getControllers();

        System.out.println("==사용가능 url==");
        for (Class<?> clazz : classList) {
            String basePath = "/";
            if (clazz.isAnnotationPresent(CommandMapping.class)) {
                basePath = clazz.getAnnotation(CommandMapping.class).value();
            }
            Object instance = clazz.getDeclaredConstructor().newInstance();  // 인스턴스 생성
            instanceMap.put(clazz.getName(), instance);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CommandMapping.class)) {
                    String fullPath = basePath + method.getAnnotation(CommandMapping.class).value();
                    methodMap.put(fullPath, method);
                    System.out.println(fullPath);
                }
            }
        }
    }

    private static void executeMethod(String url) throws ReflectiveOperationException {
        if (methodMap.containsKey(url)) {
            Method method = methodMap.get(url);
            Object instance = instanceMap.get(method.getDeclaringClass().getName());
            String result = (String) method.invoke(instance);
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
                String url = scanner.nextLine();
                if ("exit".equalsIgnoreCase(url)) {
                    break;
                }
                executeMethod(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
