package config;

import config.annotation.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApplicationContext {
    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final ComponentScanner scanner = ComponentScanner.getInstance();

    public ApplicationContext(Class<?> configClass) {
        this.registerConfiguration(configClass);
        this.autoConfiguration(configClass);
    }

    private void autoConfiguration(Class<?> configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            List<Class<?>> components = scanner.componentScanSubPackages(configClass, Component.class);
            for (Class<?> component : components) {
                String beanName = component.getSimpleName().substring(0, 1).toLowerCase() + component.getSimpleName().substring(1);
                if (!singletonBeans.containsKey(beanName)) {
                    Constructor<?> constructor = component.getDeclaredConstructors()[0];
                    if (constructor.isAnnotationPresent(Autowired.class)) {
                        Object beanInstance = createBeanInstance(constructor, components);
                        singletonBeans.put(beanName, beanInstance);
                    }
                }
            }
        }
    }

    private Object createBeanInstance(Constructor<?> constructor, List<Class<?>> components) {
        try {
            Class<?>[] parameterTypes = constructor.getParameterTypes(); //파라미터 불러와서
            Object[] initArgs = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (parameterTypes[i].isInterface()) {
                    List<Class<?>> implementations = components.stream()
                                .filter(parameterType::isAssignableFrom)
                                .toList();
                    if (implementations.isEmpty()) {
                        throw new RuntimeException("구현체를 찾을 수 없습니다: " + parameterType);
                    }
                    parameterType = implementations.get(0);
                }
                String beanName = parameterType.getSimpleName().substring(0, 1).toLowerCase() + parameterType.getSimpleName().substring(1);
                if (singletonBeans.containsKey(beanName)) { //해당 파라미터 빈 있으면 거기서 찾고
                    initArgs[i] = singletonBeans.get(beanName);
                    continue;
                }

                //없으면 직접 인스턴스 생성을 해주며 빈 등록 해야하는데
                Constructor<?> paramConstructor = parameterType.getDeclaredConstructors()[0];

                paramConstructor.setAccessible(true);
                if (paramConstructor.getParameterCount() > 0) { // 파라미터가 하나 이상 있다면
                    Object beanInstance = createBeanInstance(paramConstructor, components); //재귀
                    singletonBeans.put(beanName, beanInstance);
                    initArgs[i] = beanInstance;
                    continue;
                }
                //없으면 생성 후에 등록
                Object beanInstance = paramConstructor.newInstance();
                singletonBeans.put(beanName, beanInstance);
                initArgs[i] = beanInstance;
            }
            constructor.setAccessible(true);
            return constructor.newInstance(initArgs);
        } catch (Exception e) {
            throw new RuntimeException("빈 초기화 실패: " + constructor, e);
        }
    }

    private void registerConfiguration(Class<?> configClass) {
        try {
            Object configInstance = configClass.getDeclaredConstructor().newInstance();
            if (configClass.isAnnotationPresent(Configuration.class)) {
                configInstance = this.configure(configClass);
            }

            for (Method method : configClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Bean.class)) {
                    Object bean = method.invoke(configInstance);
                    singletonBeans.put(method.getName(), bean);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Object configure(Class<?> configClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(configClass);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            String methodName = method.getName();
            if (method.isAnnotationPresent(Bean.class) && singletonBeans.containsKey(methodName)) {
                return singletonBeans.get(methodName);
            }

            Object result = proxy.invokeSuper(obj, args);
            if (method.isAnnotationPresent(Bean.class)) {
                singletonBeans.put(methodName, result);
            }
            return result;
        });

        return enhancer.create();
    }

    public Object getBean(String beanName) {
        return singletonBeans.get(beanName);
    }

    public <T> T getBean(String beanName, Class<T> beanType) {
        return beanType.cast(singletonBeans.get(beanName));
    }

    public <T> T getBean(Class<T> beanType) {
        Optional<T> bean = singletonBeans.values().stream()
                .filter(beanType::isInstance)
                .map(beanType::cast)
                .findFirst();
        return bean.orElseThrow(() -> new IllegalArgumentException("No bean found of type " + beanType.getName()));
    }

    public String[] getBeanNames() {
        return singletonBeans.keySet().toArray(String[]::new);
    }
}