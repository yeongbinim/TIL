# 리플렉션으로 OCP 컨트롤러 만들기

이번 미션은 계산기 과제를 하다가 커맨드 명령어 하나하나 추가하면서 main함수가 변경이 되는 것에서.. 너무 불편함을 느껴 내가 직접 만든 요구사항이다. 

OCP 사나이가 되어보자!

요구사항은 아래와같다.

- 여러 메서드를 담고있는 `Controller`클래스와 main메서드가 있는 `DynamicMethodCaller`클래스로만 구성한다.
  - main에서는 콘솔로 메서드 이름을 입력받아 그에 맞는 Controller 클래스 내의 메서드가 호출되도록 한다.

- `Controller` 클래스에 메서드를 하나 추가하고, `DynamicMethodCaller` 클래스의 변경없이 추가된 메서드가 실행되도록 한다.
  - 이때, `Controller` 클래스 내부에는 사용자에 의해 실행될 메서드 이외에, 다른 메서드들은 존재하면 안된다.

<br/>

## 목차
- [ver1: 리플렉션으로 메서드 불러오기](#ver1-리플렉션으로-메서드-불러오기)
  - [런타임에 클래스 정보를 불러오는 방법, 리플렉션](#런타임에-클래스-정보를-불러오는-방법-리플렉션)
  - [구현결과 및 개선할 점](#구현결과-및-개선할-점)
- [ver2: 애노테이션으로 메서드의 메타데이터 만들기](#ver2-애노테이션으로-메서드의-메타데이터-만들기)
  - [내가 만든 애너테이션~](#내가-만든-애너테이션)
  - [구현 결과 및 개선할 점](#구현-결과-및-개선할-점)
- [ver3: 여러개의 클래스 리플렉션 하기](#ver3-여러개의-클래스-리플렉션-하기)
  - [트러블슈팅 - 자바의 애노테이션은 원래 상속이 불가능하다.](#트러블슈팅---자바의-애노테이션은-원래-상속이-불가능하다)
  - [내가만든 Component Scanner](#내가만든-component-scanner)
  - [최종 결과물](#최종-결과물)
- [새배개](#새배개)
  - [Scanner Close의 이유와, Try-with-resources 문](#scanner-close의-이유와-try-with-resources-문)
  - [리플렉션](#리플렉션)
  - [메타 애너테이션](#메타-애너테이션)
- [마치며](#마치며)

<br/>

## ver1: 리플렉션으로 메서드 불러오기

**[[ver1 코드 보러가기]](./src/dynamic1)**

전체 다이어그램과 내가 구현할 `DynamicMethodCaller` 클래스의 의사코드는 다음과 같다.  

<div align="center"><img width="400" alt="스크린샷 2024-11-14 오후 4 54 12" src="https://github.com/user-attachments/assets/b806bdf9-aced-4617-89bd-284a515c1fc2"></div>

```java
public class Controller {
  public create() {
    return "create 의 결과값";
  }
  public login() {
    return "login 의 결과값";
  }
}
```

```java
class DynamicMethodCaller {
  private AppleController controller = new AppleController();
  private Map<String, Method> methodMap = new HashMap<>();

  private initializeMapping() {
    controller에 있는 메서드들을 불러와 methodMap에 저장한다.
  }

  private executeMethod(methodName) {
    methodMap로부터 methodName을 key로 둔 Method를 실행한다.
  }

  public main() {
    initializeMapping();
    
    String methodName = 입력();
      
    String result = executeMethod(methodName);
    
    출력(result);
  }
}
```

흐름을 보면 결국 Controller에 있는 메서드들 읽어서(initializeMapping) 실행시키는 것(executeMethod)이다.

이 설계를 기반으로 initializeMapping과 executeMethod를 어떻게 구현했는지 살펴보자.

<br/>

### 런타임에 클래스 정보를 불러오는 방법, 리플렉션

코드가 실행되고 있는 시점에서, 내가 선언한 메서드들을 불러오는 방법([리플렉션](#리플렉션))은 api가 제공되고 있었기 때문에, 구현 자체는 생각보다 쉬웠다.

사용 방법은 아래와 같다.

```java
public class Temp {
  public int add(int num1, int num2) { return num1 + num2; }
}
```

Temp 클래스에 위와같은 메서드가 있다. 이것을 main메서드에서 조회하거나 호출 하려면

```java
Method method = Temp.class.getMethod("add", int.class, int.class);
method.invoke(new Temp(), 5, 3);
```

이렇게 하면 된다.

"그거는 그냥도 사용할 수 있는데, 문자열로 불러온 것 뿐이잖아 뭐가 다르다는거야?"

할 수도 있는데, 이 문자열로 조회할 수 있다는 것은 사용자로부터 입력받은 문자열로부터 메서드를 찾을 수도 있다는 것이기도 하다.

그리고, 다음 예시를 보자

```java
Method[] list = Temp.class.getDeclaredMethods();
for (Method m : methodList) {
  System.out.println(m.invoke(new Temp(), 2, 3));
}
```

일반적인 방법으로 이렇게 선언되어 있는 메서드들을 모두 호출하는 이런 함수를 작성할 수 있는가?

이걸 가능하게 하는게, 바로 이 리플렉션!

<br/>

### 구현결과 및 개선할 점

이 리플렉션을 적용하여, DynamicMethodCaller의 initializeMapping()과, 을 다음과 같이 작성했다.

```java
private static void initializeMapping() {
  System.out.println("==사용가능 메서드==");
  for (Method method : Controller.class.getDeclaredMethods()) {
    System.out.println(method.getName());
    methodMap.put(method.getName(), method);
  }
}

private static void executeMethod(String methodName) throws ReflectiveOperationException {
  if (methodMap.containsKey(methodName)) {
    Method method = methodMap.get(methodName);
    String result = (String) method.invoke(controller);
    System.out.printf("==메서드 반환값==\n%s", result);
  } else {
    System.out.println("그 메서드는 찾을 수 없어");
  }
}
```

<div align="center"><img width="350" src="https://github.com/user-attachments/assets/4918a12b-a25d-4e9f-ab93-f1dcb224f301"></div>

<br/>

여기서 아쉬운 점이 있었다.

1. method의 이름 그대로 입력해야 그 메서드가 호출된다.
2. 내가 제공하고 싶지 않은 다른 메서드들 까지 모두 조회된다.
3. 사용은 안되더라도 private으로 만든 메서드가 조회가 된다.



이 문제점들을 해결할 방법이 없을까?

<br/>

## ver2: 애노테이션으로 메서드의 메타데이터 만들기

**[[ver2 코드 보러가기]](./src/dynamic2)**



결국 '이 메서드는 사용자에게 제공할 거고, #### 라는 명령어를 쓰면 호출되게 해'라는 메타 데이터가 필요했다.

그럼 어쩔 수 없이 메서드의 메타데이터가 담긴 파일을 추가해서 관리해야 하나..? 생각이 들다가

'어? 메타데이터? 애너테이션?' 생각하며 애너테이션으로 해결할 방법이 떠올랐다.

그렇게 별도로 매핑정보가 담긴 메타데이터 파일을 만들지 않고, 메서드를 만들면 애너테이션 하나만 달아도 되도록 설계를 했다.

<br/>

### 내가 만든 애너테이션~

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandMapping {
  String value() default "/";
}

```

`@Retention`은 애너테이션이 유지되는 범위를 의미하고, `@Target`은 적용할 타겟이 메서드인지 클래스인지를 지정한다. 더 자세한 내용은 [맨 아래 학습자료](#내배개)에서 구경하자.

위와 같이 작성하면 우리 Controller에 다음과 같이 달아줄 수가 있다.

```java
public class Controller {
  @CommandMapping("/create")
  public String create() {
    return "create 의 결과값";
  }

  @CommandMapping("/login")
  public String login() {
    return "login 의 결과값";
  }
}
```

이제 메서드를 생성하고 저 CommandMapping에 원하는 명령어를 넣으면, 런타임에도 메타데이터가 반영이 되는 것이다!

저 애노테이션을 달았는지 확인하고, 값을 조회하는 방법은 아래와 같다.

```java
boolean isCommand = method.isAnnotationPresent(CommandMapping.class);
String url = method.getAnnotation(CommandMapping.class).value();
```

<br/>

### 구현 결과 및 개선할 점

이제 이걸 기반으로 initializeMapping을 다시 구현 해보자.

해당 애너테이션이 있는지 확인하고, 애너테이션 안에 있는 값을 조회하는 코드를 추가하면 될 것 같다.

```java
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
```

<div align="center"><img width="350" alt="스크린샷 2024-11-14 오후 8 14 00" src="https://github.com/user-attachments/assets/5a7a0f9f-17c3-45ec-b0ab-48abd2cce222"></div>

우리가 등록한 "/create"라는 메타데이터를 통해 함수를 호출한 것을 확인할 수 있다.



하지만 그럼에도 아직 아쉬운 부분이 남았다.

1. Controller가 하나가 아니라 여러개라면 main함수에 다시 추가해야되지 않나.
2. Controller가 하나가 아니라 여러개라면 RequestMapping을 Class에 붙이고 싶다.

이 결핍을 해결해보자



<br/>

## ver3: 여러개의 클래스 리플렉션 하기

**[[ver3 코드 보러가기]](./src/dynamic3)**

`@Controller` 라는 애너테이션을 만들어서 클래스에 붙일 수 있도록 설정하면, 이 애너테이션이 붙은 클래스들을 리플렉션할 수 있을 것 같다.

우선 `@Controller` 애너테이션을 만들어보자

<br/>

### 트러블슈팅 - 자바의 애노테이션은 원래 상속이 불가능하다.

스프링처럼 @Container 말고도 @Service @Repository 들이 @Component를 상속받는 것을 흉내내려 했다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Controller {
}
```

위와 같이 @Component 애너테이션을 만들고, @Controller위에 이걸 달아서, 리플렉션할때 @Component를 조회하도록 했다.

하지만, Controller 조회가 되지 않았고, 알아보니 Java의 애너테이션은 애너테이션끼리 상속되는 것을 제공하지 않는다고 한다.

스프링에서.. 뭔가 어떻게 했겠지... 나도 어떻게 할 수는 있을거 같은데, 그거 처리하는 것 까지 하면 너무 목적과 벗어나는 것 같아서 우선은 Controller들만 리플렉션 하도록 했다.

<br/>

### 내가만든 Component Scanner

클래스를 조회하기 위해서는 컴파일된 상태의 class파일 위치를 직접 알아내서 URLClassLoader로 로드해야 한다.

재귀적으로 하면 하위 패키지까지 조회가 모두 가능할 것 같기는 했지만, 현재로써는 그냥 현재 패키지에 있는 `@Controller`들만 조회 하게 했다.

구현 코드는 아래와 같다.

```java
private List<Class<?>> componentScan(Class<? extends Annotation> componentScanClass) throws IOException, ClassNotFoundException {
  String packageName = getClass().getPackage().getName(); // (1)
  String path = packageName.replace('.', '/'); // (2)
  File dir = new File("build/classes/java/main/" + path); // (3)
  List<Class<?>> components = new ArrayList<>(); //(4)
  URL[] urls = {dir.toURI().toURL()}; //(5)

  try (URLClassLoader loader = new URLClassLoader(urls)) { // (6)
    for (File file : Objects.requireNonNull(dir.listFiles())) { // (7)
      if (file.getName().endsWith(".class")) { //(8)
        String className = file.getName().substring(0, file.getName().length() - 6);
        Class<?> clazz = loader.loadClass(packageName + "." + className); //(9)
        if (clazz.isAnnotationPresent(componentScanClass)) {
          components.add(clazz); //(10)
        }
      }
    }
  }
  return components;
}
```

**(1) 현재 클래스의 패키지 이름 얻기:**

- `getClass().getPackage().getName()`은 메서드가 실행 중인 클래스의 패키지 이름을 문자열로 반환한다. 
- 클래스 로더가 클래스를 찾을 수 있는 경로를 만드는 데 필요한 기본 정보.

**(2) 패키지 이름을 경로로 변환:**

- `packageName.replace('.', '/')`는 패키지 이름에서 점(`.`)을 슬래시(`/`)로 변경하여 파일 시스템에서 사용할 수 있는 경로 형식으로 변환한다. 
- 예를 들어, `com.example.test`는 `com/example/test`로 변환된다.

**(3) 디렉토리 경로 생성:**

- `new File("build/classes/java/main/" + path)`는 이전 단계에서 변환된 경로를 기반으로 실제 파일 시스템의 디렉토리 경로를 생성한다.
- 이 경로는 프로젝트의 빌드 디렉토리 안에 있는 클래스 파일들을 가리킨다.

**(4) 클래스 리스트 초기화:**

- `new ArrayList<>()`는 발견된 컴포넌트들을 저장할 `ArrayList`를 생성한다.
- 이 리스트는 검색된 클래스 객체들을 담게 된다.

**(5) URL 배열 생성:**

- `dir.toURI().toURL()`은 파일 경로를 `URL` 객체로 변환한다.
- 이 URL은 클래스 로더가 클래스 파일을 로드하는 데 사용된다.
- `URL[] urls` 배열은 클래스 로더 생성에 필요한 URL을 담고 있다.

**(6) URL 클래스 로더 생성 및 자원 관리:**

- `new URLClassLoader(urls)`는 주어진 URL 배열을 사용하여 새로운 `URLClassLoader` 인스턴스를 생성한다.
- 이 클래스 로더는 지정된 URL에서 클래스를 로드할 수 있다.

**(7) 디렉토리의 파일 리스트 조회:**

- `dir.listFiles()`는 위에서 지정한 디렉토리 안의 모든 파일을 나열한다.

**(8) 클래스 파일 검사:**

- `file.getName().endsWith(".class")`는 파일 이름이 `.class` 확장자로 끝나는지를 검사하여 클래스 파일만을 처리하도록 한다.

**(9) 클래스 로드:**

- `loader.loadClass(packageName + "." + className)`는 클래스 로더를 사용하여 파일 이름으로부터 구성된 전체 클래스 이름을 로드한다.
- 패키지 이름과 클래스 이름을 조합하여 올바른 클래스 이름을 만든다.

**(10) 어노테이션 존재 확인 및 리스트에 추가:**

- `clazz.isAnnotationPresent(componentScanClass)`는 로드된 클래스에 특정 어노테이션이 존재하는지 검사한다.
- 만약 해당 어노테이션이 존재한다면, `components` 리스트에 클래스를 추가한다.



처음 써보는 API들이라 한 줄 한 줄 디버깅 해가면서 각 줄마다 어떤 객체가 생기고, 어떤 값이 도출되는지 확인해보면서 1시간 넘게 작성했다.

여튼 이 함수에 스캔하고 싶은 애너테이션을 넘기면 스캔하여 반환하고, initializeMapping 중에 이 컴포넌트 스캐너를 활용하여, 원하는 메서드들을 매핑한다.

initializeMapping 변경사항은... 코드가 생각보다 길어서 코드를 직접 확인해보자!

<br/>

### 최종 결과물

만든 애너테이션들을 모두 조합해서 사용해보자.

크게 AppleController와, BananaController로 나누었다. 그 중 AppleController는 아래처럼 작성했다. (BananaController 도 거의 동일)

```java
@Controller
@CommandMapping("/apple")
public class AppleController {
    @CommandMapping("/create")
    public String create() {
        return "Apple create 의 결과값";
    }

    @CommandMapping("/login")
    public String login() {
        return "Apple login 의 결과값";
    }
}

```

<div align="center"><img width="350" alt="스크린샷 2024-11-15 오전 1 12 23" src="https://github.com/user-attachments/assets/6ebd4ad6-8f91-4845-a434-a4131e04e7f8"></div>

Controller와의 의존관계가 아예 사라졌으며, main 메서드의 변경 없이 컨트롤러를 만들어서 @Controller, @CommandMapping 만 잘 적어주면, 우리가 원하는 명령어와 함수가 연결이 된다.

<br/>

## 새배개

### Scanner Close의 이유와, Try-with-resources 문

scanner를 사용하면 항상 close 를 사용하라한다. 보통 아래와 같이 작성한다.

```java
Scanner scanner = new Scanner(System.in);
try {
	//로직 들어가는 곳
} catch (Exception e) {
  e.printStackTrace();
} finally {
  scanner.close();
}
```

scanner 객체는 내부적으로 입력 스트림과 연결되어 있는 자원을 사용하는데, 이 스트림을 열어 놓고 닫지 않으면 해당 파일이나 소켓을 계속 사용하고 있는 것으로 간주된다.

close 하지 않을 경우, 메모리 누수 문제가 있을 수 있고, 파일을 입력 소스로 사용하는 경우 파일 락을 해제하지 않을 것이며, Scanner 내부적으로 사용하는 버퍼가 정리되지 않을 것이다.

따라서 finally문에 close를 꼭 해주는데, 아래처럼 생략할 수가 있다.

```java
try(Scanner scanner = new Scanner(System.in)) {
	//로직 들어가는 곳
} catch (Exception e) {
  e.printStackTrace();
}
```

구문은 Java에서 "try-with-resources" 문이라고 부르며

Java 7부터 도입, 이를 사용하면 `try` 블록을 벗어날 때 자동으로 자원을 해제할 수 있다.

<br/>

### 리플렉션

JVM은 **클래스 정보를 클래스 로더를 통해 읽어와서 해당 정보를 JVM 메모리에 저장**한다. 리플렉션을 사용하면 **생성자, 메소드, 필드 등 클래스에 대한 정보에 실행 중 접근할 수 있다**.

리플렉션을 사용하면 클래스와 메소드에 어떤 애노테이션이 붙어 있는지 확인할 수 있다. 애노테이션은 그 자체로는 아무 역할도 하지 않지만, 리플렉션 덕분에 스프링에서 `@Component` , `@Bean` 과 같은 애노테이션에 기능이 있는 것처럼 사용할 수 있다.

또한, 인텔리제이와 같은 IDE에서 Getter, Setter를 자동으로 생성해주는 기능도 리플렉션을 사용하여 필드 정보를 가져와 구현한다고 한다.

<br/>

일반적으로 메소드를 호출한다면, 컴파일 시점에 분석된 클래스를 사용하지만 리플렉션은 **런타임에 클래스를 분석하므로 속도가 느리다**. 그리고 이런 특징으로 인해 **타입 체크가 컴파일 타임에 불가능**하다.

따라서 일반적인 웹 애플리케이션 개발자는 사실 리플렉션을 사용할 일이 거의 없다. 보통 라이브러리나 프레임워크를 개발할 때 사용된다. 따라서 **정말 필요한 곳에만 리플렉션을 한정적으로 사용**해야한다.

<br/>

### 메타 애너테이션

애너테이션을 사용하면,
매핑 정보(자바 객체 사이던, 다른 프로그램과 자바 객체의 사이던..)를 클래스 내에 직접 명시할 수가 있어서, 별도의 매핑 정보가 담긴 파일을 만들지 않아도 된다.

메타 애너테이션(애너테이션을 위한 애너테이션)을 사용하면,
애너테이션을 정의할 때 이 애너테이션이 어떻게 적용될지 정보를 담을 수 있다.

- `@Target` : 이 애너테이션이 적용 가능한 대상을 지정한다.

- `@Retention`: 애너테이션이 유지되는 기간을 지정한다. 3개의 정책이 있다.

  - `RetentionPolicy.SOURCE`: Override처럼 소스 파일에만 존재하며, 컴파일 된 이후의 클래스파일에는 존재하지 않는다. 컴파일러 개발자가 아니라면 쓸 일 없다.
  - `RetentionPolicy.RUNTIME`: 컴파일 이후 클래스 파일에도 존재하며, 실행 시에 리플렉션을 통해 클래스 파일에 저장된 애너테이션 정보를 읽어 처리 가능하다.
  - `RetentionPolicy.CLASS`: **default값**. 컴파일 이후 클래스 파일에 저장되지만, JVM에 로딩될 때는 무시되기때문에 기본값임에도 잘 사용되지 않는다.

- `@Documented`: 애너테이션에 대한 정보가 javadoc으로 작성한 문서에 포함되도록 한다.

- `@Inherited`: 이 애너테이션을 붙여 작성한 애너테이션은 조상 클래스에 붙였을때, 자손 클래스도 이 애너테이션 적용이 된다.

  ```java
  @Inherited
  @interface SupperAnno {}
  
  @SuperAnno
  class Parent {}
  
  class Child extends Parent {} // 이 클래스도 @SuperAnno가 적용된다.
  ```

- `@Repeatable`: 보통은 하나의 대상에 한 종류의 애너테이션을 붙이는데, 이 애너테이션이 붙은 애너테이션은 여러 번 붙일 수 있다. 이 애너테이션들을 하나로 묶어서 다룰 수 있는 애너테이션도 정의해야 한다.

  ```java
  @interface ToDos {
    ToDo[] value();
  }
  
  @Repetable(ToDos.class)
  @interface Todo {
    String value();
  }
  
  @Todo("할 일1")
  @Todo("할 일2")
  class Myclass{}
  ```


<br/>


## 마치며

누가 보더라도, 스프링의 향기가 많이 난다.

실제로 의존관계를 해결하려고 애를 쓰다보니 '스프링은 이걸 어떻게 해결했을까?' 하면서 습관처럼 적었던 애너테이션에 관심을 갖게 되었다.

리플렉션 없이 static method를 통해 의존관계를 약화시킨다거나, 구성정보를 관리하는 파일도 따로 관리해봤는데, 이거만큼 편한게 없다.

스프링이 왜 이 구조를 가지게 되었는지 조금은 이해가 된다.

<br/>

크게 느낀건 아직 예외처리가 많이 부족하다...

예외 처리에 대해서 하루동안 진득한 시간을 가져서 TIL 을 해봐야겠다.
