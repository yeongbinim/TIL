package config;

import config.annotation.Bean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ApplicationContext {
    private final Map<String, Object> singletonBeans = new HashMap<>();

    public ApplicationContext(Class<?> configClass) {
        this.register(configClass);
    }

    private void register(Class<?> configClass) {
        try {
            Object configInstance = configClass.getDeclaredConstructor().newInstance();

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
}