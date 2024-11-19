package config;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class ComponentScanner {
    private static ComponentScanner instance;
    private final List<Class<?>> components;

    public static ComponentScanner getInstance() {
        if (instance == null) {
            instance = new ComponentScanner();
        }
        return instance;
    }

    /**
     * 애노테이션이 적용된 모든 클래스를 필터링하여 반환합니다.
     *
     * @param annotation 필터링할 어노테이션 클래스. 이 어노테이션이 적용된 클래스만을 리스트에서 찾아 반환합니다.
     * @return 어노테이션이 적용된 클래스의 리스트. 만약 어떤 클래스도 해당 어노테이션을 가지고 있지 않다면 빈 리스트가 반환됩니다.
     */
    public List<Class<?>> componentScan(Class<? extends Annotation> annotation) {
        return components.stream()
                .filter((clazz) -> clazz.isAnnotationPresent(annotation))
                .toList();
    }

    public List<Class<?>> componentScanSubPackages(Class<?> baseClass, Class<? extends Annotation> annotation) {
        String basePackage = baseClass.getPackage().getName();
        return components.stream()
                .filter(clazz -> clazz.getPackage().getName().startsWith(basePackage))
                .filter(clazz -> clazz.isAnnotationPresent(annotation))
                .toList();
    }

    private ComponentScanner() {
        components = new ArrayList<>();
        String packageName = "ver2";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(packageName);
            while (resources.hasMoreElements()) {
                processResource(resources.nextElement(), packageName, classLoader);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("초기화 중 예외 발생", e);
        }
    }

    private void processResource(URL resource, String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        if (resource.getProtocol().equals("jar")) {
            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
            scanJar(jarPath, packageName, classLoader);
        } else {
            scanDir(new File(resource.getFile()), packageName);
        }
    }

    private void scanJar(String path, String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        try (JarFile jar = new JarFile(path)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && entry.getName().startsWith(packageName)) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    components.add(clazz);
                }
            }
        }
    }

    private void scanDir(File directory, String packageName) throws ClassNotFoundException {
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            if (file.isDirectory()) {
                String subPackageName = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                scanDir(file, subPackageName);
            }
            else if (file.getName().endsWith(".class")) {
                String fullClassName = packageName + '.' + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(fullClassName);
                components.add(clazz);
            }
        }
    }
}
