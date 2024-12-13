package dynamic3;

import dynamic3.annotation.Controller;
import dynamic3.annotation.Service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComponentScanner {
    private static ComponentScanner instance;

    private ComponentScanner() {}

    public static ComponentScanner getInstance() {
        if (instance == null) {
            instance = new ComponentScanner();
        }
        return instance;
    }

    private List<Class<?>> componentScan(Class<? extends Annotation> componentScanClass) throws IOException, ClassNotFoundException {
        String packageName = getClass().getPackage().getName();
        String path = packageName.replace('.', '/');
        File dir = new File("build/classes/java/main/" + path);  // 클래스 파일이 위치한 디렉토리
        List<Class<?>> components = new ArrayList<>();
        URL[] urls = {dir.toURI().toURL()};

        try (URLClassLoader loader = new URLClassLoader(urls)) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.getName().endsWith(".class")) {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = loader.loadClass(packageName + "." + className);
                    if (clazz.isAnnotationPresent(componentScanClass)) {
                        components.add(clazz);
                    }
                }
            }
        }
        return components;
    }

    public List<Class<?>> getControllers() throws IOException, ClassNotFoundException {
        return componentScan(Controller.class);
    }

    public List<Class<?>> getServices() throws IOException, ClassNotFoundException {
        return componentScan(Service.class);
    }
}
