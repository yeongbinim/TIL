# Java 애너테이션 상속 불가

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
