# Scanner Close의 이유, Try-with-resources 문

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
