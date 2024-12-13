# Meta Annotation

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
