package config;

import config.annotation.Controller;
import config.annotation.Service;

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

    private void scanDirectory(File directory, String packageName, Class<? extends Annotation> componentScanClass, List<Class<?>> components, URL[] urls) throws IOException, ClassNotFoundException {
        try (URLClassLoader loader = new URLClassLoader(urls)) {
            File[] files = Objects.requireNonNull(directory.listFiles());
            for (File file : files) {
                if (file.isDirectory()) { // 하위 디렉토리 재귀적으로 스캔
                    String subPackageName = packageName + "." + file.getName();
                    scanDirectory(file, subPackageName, componentScanClass, components, urls);
                } else if (file.getName().endsWith(".class")) {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = loader.loadClass(packageName + "." + className);
                    if (clazz.isAnnotationPresent(componentScanClass)) {
                        components.add(clazz);
                    }
                }
            }
        }
    }

    private List<Class<?>> componentScan(Class<? extends Annotation> componentScanClass) throws IOException, ClassNotFoundException {
        String packageName = getClass().getPackage().getName();
        int lastDotIndex = packageName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            packageName = packageName.substring(0, lastDotIndex);
        }
        String path = packageName.replace('.', '/');
        File dir = new File("build/classes/java/main/" + path);  // 클래스 파일이 위치한 디렉토리
        List<Class<?>> components = new ArrayList<>();
        URL[] urls = {dir.toURI().toURL()};
        scanDirectory(dir, packageName, componentScanClass, components, urls);
        return components;
    }

    public List<Class<?>> getControllers() throws IOException, ClassNotFoundException {
        return componentScan(Controller.class);
    }

    public List<Class<?>> getServices() throws IOException, ClassNotFoundException {
        return componentScan(Service.class);
    }
}
