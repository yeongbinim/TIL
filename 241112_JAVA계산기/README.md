# JAVA 계산기

어제 강의에서 제공하는 숙제? 는 다 하긴 했는데, 별도의 과제가 있어서 처음부터 다시 하게 되었다.

<br/>

입력 받을때 어떤 함수를 사용할 건지, 어떤 클래스를 구현해야 하는지, 어떤 순서로 입력을 받아야 하는지..

요구사항이 너무나도 명확하기 때문에 해당 요구사항에 최대한 맞춰서 개발 했다.

<br/>

최신 버전은 [[241120_JAVA제네릭계산기]](../241120_JAVA제네릭계산기) 에 있습니다.
아래 내용은 11월 12일에 작성한 버전입니다.

<br/>

## 목차
- [Level1: 절차지향계산기](#level1-절차지향계산기)
  - [정규표현식을 사용하여 입력 검증](#정규표현식을-사용하여-입력-검증)
  - [프로시저 추상화](#프로시저-추상화)
- [Level2: 객체지향계산기](#level2-객체지향계산기)
  - [Operation 인터페이스와 구현 클래스](#operation-인터페이스와-구현-클래스)
  - [Calculator 클래스의 비즈니스 네이밍](#calculator-클래스의-비즈니스-네이밍)
  - [제네릭으로 타입 안정성 확보하기](#제네릭으로-타입-안정성-확보하기)
- [Level3: enum계산기](#level3-enum계산기)
  - [enum으로 묶기](#enum으로-묶기)
  - [람다식을 이용한 사칙연산](#람다식을-이용한-사칙연산)
  - [stream으로 필터링하기](#stream으로-필터링하기)
  - [실수를 검증하는 정규표현식](#실수를-검증하는-정규표현식)
- [아직 해결 못한 점](#아직-해결-못한-점)
- [마치며](#마치며)

<br/>

## Level1: 절차지향계산기

CLI환경에서 사용자로부터 정수 2개와 연산자를 입력받아 연산을 하는 계산기 프로그램을 절차지향적으로 작성해야 한다.

단, 이때 음의정수는 받을 수 없으며 실수도 안된다는 조건이다.

<div align="center">
<img width="600" alt="스크린샷 2024-11-12 오후 9 56 45" src="https://github.com/user-attachments/assets/3728b500-4d66-48c9-b6ad-72a322f17520">
</div>

[[calc1 코드 보러가기]](./src/calc1/)

구현한 프로그램의 결과물이다.

<br/>

### 정규표현식을 사용하여 입력 검증

프로그램이 돌아가는 과정을 일반화 해보면, `입력 -> 검증 -> 처리 -> 포매팅 -> 출력` 과정을 거친다고 생각한다.

<br/>

난 여기서 입력 이후 값을 검증할 때 정규 표현식을 자주 사용하는 편이다.

<br/>

정규 표현식은 '**문자열에서 원하는 부분을 일치시킨다**'는 명확한 1가지의 책임을 가지고 있고,

일치시킨 부분을 치환하거나, 검증하거나, 추출하는 일은 전문가이며 거의 모든 언어가 정규 표현식을 지원하기 때문에 익숙해지면 이것만큼 편한 것도 없다.

```java
char operator scanner.next("[+\\-*/%]").charAt(0);
int number Integer.parseInt(scanner.next("[0-9]+"));
```

scanner.next()의 인자로 정규표현식을 넘길 수 있는 점을 활용했다.

'number는 솔직히 nextInt() 쓰는게 낫잖아?' 싶을텐데,

나는 아래의 두 가지 이유를 갖고 next()를 통일했다.

1. 0과 양의 정수만을 입력 받아야 한다.
2. 공통적으로 next()를 사용하는 함수로 묶기 위해서이다.

이제 함수로 묶는 부분(= 프로시저 추상화)을 살펴보자.

<br/>

### 프로시저 추상화

프로그램이 유지보수 되기 위해서 나온 첫번째 패러다임은 프로시저(절차)를 추상화 하는 것이다.

복잡한 프로시저가 있을때, 이것의 작성을 매번 반복하는 것이 아니라 추상화 시켜 특정 메모리에 올리고, 필요할때 사용한다.

<br/>

이것이 함수라고 이해했다.

<br/>

내 코드에서 가장 많이 반복되는 코드는 아래였다.

```java
System.out.print("명령을 입력하세요 (exit: 종료, calc: 연산): ");
while (!scanner.hasNext("exit|calc")) {
    System.out.print("잘못된 명령입니다. 'exit' 또는 'calc' 중 하나를 입력하세요: ");
    scanner.next(); // 잘못된 입력 넘기기 위함
}
String command = scanner.next();
...
System.out.print("첫번째 양의 정수를 입력하세요: ");
while (!scanner.hasNext("[0-9]+")) {
    System.out.print("양의 정수가 아닙니다. 다시 입력해 주세요: ");
    scanner.next();
}
int num1 = Integer.parseInt(scanner.next());

System.out.print("연산 기호를 입력하세요(+-*/%): ");
while (!scanner.hasNext("[+\\-*/%]")) {
    System.out.print("허용된 기호가 아닙니다. 다시 입력해 주세요: ");
    scanner.next();
}
char operator = scanner.next().charAt(0);

System.out.print("두번째 양의 정수를 입력하세요: ");
while (!scanner.hasNext("[0-9]+")) {
    System.out.print("양의 정수가 아닙니다. 다시 입력해 주세요: ");
    scanner.next();
}
int num2 = Integer.parseInt(scanner.next());
```

반복되는 부분을 함수로 묶고, 안내 prompt, 에러 prompt, 정규 표현식 pattern 3개를 매개변수로 받아 사용자의 입력을 반환하도록 했다.

```java
private static String input(String prompt, String wrongPrompt, String pattern) {
  System.out.print(prompt);
  while (!scanner.hasNext(pattern)) {
      System.out.print(wrongPrompt);
      scanner.next();
  }
  return scanner.next();
}
...
String command = input(
  "명령을 입력하세요 (exit: 종료, calc: 연산): ",
  "잘못된 명령입니다. 'exit' 또는 'calc' 중 하나를 입력하세요: ",
  "exit|calc");
int num1 = Integer.parseInt(input(
  "첫번째 양의 정수를 입력하세요: ",
  "양의 정수가 아닙니다. 다시 입력해 주세요: ",
  "[0-9]+"));
char operator = input(
  "연산 기호를 입력하세요(+-*/%): ",
  "허용된 기호가 아닙니다. 다시 입력해 주세요: ",
  "[+\\-*/%]").charAt(0);
int num2 = Integer.parseInt(input(
  "두번째 양의 정수를 입력하세요: ",
  "양의 정수가 아닙니다. 다시 입력해 주세요: ",
  "[0-9]+"));
```

코드를 확장해나가기 더 좋아졌다고 판단했다.

이외에도 사칙연산하는 코드도 추상화하였다.

추가로 javaDoc을 사용하여 주석을 작성했다.

<br/>

## Level2: 객체지향계산기

Level2에서는 Calculator 클래스를 추가적으로 구현해야 한다.

<br/>

그리고 이 Calculator 클래스는 사칙연산 결과들을 Collection 자료형에 누적하고 있으며

사용자의 요청에 따라 앞 또는 뒤의 결과를 지울 수 있어야 한다.

<div align="center">
<img width="600" alt="스크린샷 2024-11-12 오후 9 32 45" src="https://github.com/user-attachments/assets/a6d3cf30-a79c-468f-9a5d-f2efd3a79454">
</div>

[[calc2 코드 보러가기]](./src/calc2/)

위는 내가 구현한 계산기의 실행 결과이다.

<br/>

사용자의 요청에 따라 지우는 기능에 대해 계산기에 실행취소 기능이 있다고 생각했고,

가장 오래된 데이터를 제거하는건.. 어떤 기능인지 따로 떠오르지가 않아 Calculator 클래스에 메서드는 만들어 두었지만 콘솔 인터페이스에 추가하지는 않았다.

<br/>

### Operation 인터페이스와 구현 클래스

```java
public class Calculator {
    public double calculate(int num1, int num2, char operator) {
      return switch (operator) {
          case '+' -> num1 + num2;
          case '-' -> num1 - num2;
          case '*' -> num1 * num2;
          case '/' -> (double) num1 / num2;
          case '%' -> num1 % num2;
          default -> 0.0;
      };
    }
}
```
처음에는 그냥 이렇게 구현했다. 

<br/>

얼마나 깔끔하고 좋은가. 솔직히 말하자면 지금 설명할 인터페이스와 구현체 설계도보다 유지보수가 훨씬 더 좋다.

<br/>

이게 더 유지보수에 좋다고 생각하는 이유는 숫자끼리의 연산에서 저 5개를 제외하고 크게 추가할 일이 없기 때문이고

흔히들 각 연산들에 대해서 `+-*/%` 저 연산자들을 떠올리기 때문이다.

<br/>

하지만, 우리가 상상치도 못하는 더 많은 연산들이 추가 될 예정이고 (예를 들면 루트, 로그, 제곱, ...)

각 연산자들이 추가될때 이 **Calculator 코드의 변경 없이 확장이 가능하도록** (OCP원칙을 준수하도록) 작성하고 싶다고 가정하자는 것이다.

<br/>

그렇다면 이런 설계도를 가지게 된다.

<div align="center">
  <img src="https://github.com/user-attachments/assets/ff43185f-4ce1-4b0a-b3b9-c3a4679f301a" width="600"/>
</div>

스파르타 강의자료에서 첨부한 그림이긴 한데, 나는 저 AbstractOperation이 아닌 Operation 인터페이스로 했다는 차이점이 있다.

<br/>

**공통된 구현 메서드를 공유하지 않는 지금**의 상황에서 **인터페이스가 더 목적에 맞다**고 판단했기 때문이다.

```java
public interface Operation {
    double operate(int num1, int num2);
}
```

어쨌든 이 인터페이스를 만들고 각 구현체들이 구현하도록 했다.

```java
public class Calculator {
    private final ArrayDeque<Double> resultDeque = new ArrayDeque<>();

    public double calculate(int num1, int num2, Operation operation) {
        double result = operation.operate(num1, num2);
        resultDeque.addLast(result);
        return result;
    }
}
```

그리고 Calculator 클래스가 `calculate()`를 실행하는 시점에서 Operation 인터페이스를 통해 주입된 인스턴스를 실행하도록 구현했다.

<br/>

**인터페이스에 의존하게끔 해서 DIP 원칙**을 지킨 것이다.

<br/>

### Calculator 클래스의 비즈니스 네이밍

Calculator 클래스에서 컬렉션을 뺴고 조회하는 메서드들의 네이밍에 신경을 썼다.

removeLast(), removeFirst(), toArray

이런식으로 짓지 않으려고 했다.

```java
public class Calculator {
    private final ArrayDeque<Double> resultDeque = new ArrayDeque<>();

    // 중략

    public void discardOldest() {
        if (!resultDeque.isEmpty()) {
            resultDeque.pollFirst();
        }
    }

    public void undo() {
        if (!resultDeque.isEmpty()) {
            resultDeque.pollLast();
        }
    }

    public Double[] getHistory() {
        return resultDeque.toArray(Double[]::new);
    }
}
```

Calculator의 클래스의 연산 메소드가 operate가 아닌 calculate인 것처럼,

비즈니스 관점에서 생각해서 undo, discardOldest, getHistory 이렇게 네이밍을 했다.

<br/>

Operation.operate()를 바로 사용하지 않고 Calculator를 굳이 경유해서 연산을 하는 이유도 이것 때문이라고 생각했다.

<br/>

### 제네릭으로 타입 안정성 확보하기

여기에 제네릭을 적용할 이유가 있는가? 그냥 Number로 다 받으면 되는거 아닌가? 싶었는데, 일단 요구사항대로 구현을 해 봤다.

```java
public interface Operation <T extends Number> {
    double operate(T num1, T num2);
}
```

```java
public class Calculator<T extends Number> {
  private final ArrayDeque<Double> resultDeque = new ArrayDeque<>();

  public double calculate(T num1, T num2, Operation<T> operation) {
      double result = operation.operate(num1, num2);
      resultDeque.addLast(result);
      return result;
  }
}
```

이런식으로 구현을 했는데, 음... 굳이? 싶다.

어떤 이점이 있는가.

<br/>

내가 아는 제네릭은 사실 '**반환**'에 초점을 뒀다.

**어떤 입력 타입을 넣었는지 모르니, 어떤 반환 타입이 나올지 모른다**는 점. 따라서 이때 제네릭을 사용해야 한다.

<br/>

하지만, 이 경우에는 반환 타입이 확실하게 정해질 수밖에 없다. 왜 그런지 아래를 보자

```java
public class AddOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        return num1.doubleValue() + num2.doubleValue();
    }
}
```

아무리 T extends Number라고 하더라도 Number 타입 자체는 사칙연산이 불가능하기 때문에,

여기서 결국 연산을 위해서는 doubleValue()든 intValue()든 연산을 위한 값으로 변환이 되어야 한다.

<br/>

따라서 반환 타입이 정해져 있는데 굳이굳이 제네릭을 쓴다? 아.. 모르겠다. 그래도 난 썼다!

<br/>

물론 반환 타입이 정해져 있는 메서드가 하나라도 있다면? 바로 납득.

<br/>

## Level3: enum계산기

마지막 Level3에서 추가된 요구사항은 다음과 같다.

1. Enum 타입을 활용하여 연산자 타입 정보 관리하는 것
2. 사용자로부터 double을 입력 받아도 수행되는 것 (이때 제네릭 사용)
3. 저장된 연산들 중 사용자에게 입력받은 수 보다 큰 결과물 출력되도록 하는 기능 구현 (stream, lambda)

여기서 제네릭은 위에서 어느정도 시행착오를 겪어서 이번 레벨에서는 Enum 타입을 활용하는데 집중했다.

<div align="center">
<img width="600" alt="스크린샷 2024-11-12 오후 11 14 49" src="https://github.com/user-attachments/assets/ba90819e-e31d-4829-a953-47188284ec44">
</div>

[[calc3 코드 보러가기]](./src/calc3/)

위는 내가 구현한 level3 계산기의 실행 결과이다.

<br/>

### enum으로 묶기

enum을 아래처럼만 알고 있었어서 처음에는 어떻게 활용하라는 거지? 했다.

```java
public enum OperatorType {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    REMAINDER;
}
```

자.. 이걸로 뭘 할까 몇 분을 멍때리고는 Java의 정석을 펼쳐 enum을 배웠다.

놀라운 점은 클래스처럼 생성자가 가능하다는 점, 아니 enum이 사실은 클래스라는 점

저 ADD, SUBTRACT, MULTIPLY, ... 들은 각각이 인스턴스인 셈이다.

그래서 고유할 수 있고, '열거된 싱글톤 인스턴스' 이게 내가 오늘 경험한 enum 소감이다.

할 수 있는 많은 것들이 떠올랐다.

```java
public enum OperatorType {
    ADD('+', new AddOperation<>()),
    SUBTRACT('-', new SubstractOperation<>()),
    MULTIPLY('*', new MultiplyOperation<>()),
    DIVIDE('/', new DevideOperation<>()),
    REMAINDER('%', new RemainderOperation<>());

    private final char operator;
    private final Operation<Number> operation;

    OperatorType(char operator, Operation<Number> operation) {
        this.operator = operator;
        this.operation = operation;
    }

    public double operate(Number value1, Number value2) {
        return operation.operate(value1, value2);
    }

    public boolean equals(char operator) {
        return this.operator == operator;
    }

    public static OperatorType getOperatorType(char operator) {
        for (OperatorType calculatorType: OperatorType.values()) {
            if (calculatorType.equals(operator)) {
                return calculatorType;
            }
        }
        return ADD;
    }
}
```

가장 고민이 되었던 부분이 '%' 이 기호와 나머지연산을 하는 RemainderOperation을 어떻게 같이 묶지?(main메서드를 개발하는 개발자가 기존의 상식 없이 '아 둘이 관계가 있구나!' 싶도록) 였다.

팩토리 패턴을 사용해야 하나? 생각도 했는데, 그것도 결국 확장성이 떨어질 것 같았다.

<br/>

저 중간에 getOperatorType을 보면 정말 아름답다. OperatorType.values() 로 지금 생성되어 있는 싱글턴 인스턴스(Enum) 들을 모두 조회할 수가 있다.

이건 enum이라는 열거형의 특징 때문에 가능한 것이었다.

<br/>

이걸 통해 사용하는 코드가 다음과 같이 바뀌었다.

```java
//변경 전
Map<Character, Operation<Integer>> operationMap = new HashMap<>(){{
  put('+', new AddOperation<>());
  put('-', new SubstractOperation<>());
  put('*', new MultiplyOperation<>());
  put('/', new DevideOperation<>());
  put('%', new RemainderOperation<>());
}};
double result = calculator.calculate(num1, num2, operationMap.get(operator));
```
```java
// 변경 후
double result = calculator.calculate(num1, num2, OperatorType.getOperatorType(operator));
```

main메서드 개발자는 더이상, 어떤 기호가 Operation이 매칭되는지 일일이 정하지 않아도 된다.

그것을 정의한 OperatorType이 있기 때문이다.

<br/>

### 람다식을 이용한 사칙연산

객체지향의 다형성을 연습해본다는 데에 의의를 뒀었지만, 이미 구현한 부분에 대해서는 자유롭게 변경이 가능하다고 해서 기존 Operation들을 버리고 람다식으로 바꿔봤는데,

이때 java.util.function 패키지(자주 쓰이는 형식의 메서드를 함수형 인터페이스로 미리 정의해둔 패키지)의 `BiFunction<T, U, R>` 인터페이스를 사용했다.

```java
import java.util.function.BiFunction;

public enum OperatorType {
    ADD('+', (value1, value2) -> value1.doubleValue() + value2.doubleValue()),
    SUBTRACT('-', (value1, value2) -> value1.doubleValue() - value2.doubleValue()),
    MULTIPLY('*', (value1, value2) -> value1.doubleValue() * value2.doubleValue()),
    DIVIDE('/', (value1, value2) -> value1.doubleValue() / value2.doubleValue()),
    REMAINDER('%', (value1, value2) -> value1.doubleValue() % value2.doubleValue());

    private final BiFunction<Number, Number, Double> expression;
    private final char operator;

    OperatorType(char operator, BiFunction<Number, Number, Double> expression) {
        this.operator = operator;
        this.expression = expression;
    }
}
```

작성하고 비교해보니, 각각의 Operation 인스턴스를 생성하는 것 보다는 확실히 이게 더 좋은 것 같다.

여기서 내린 객체지향을 쓰는 내 기준 2가지

1. 제공되는 기능을 위해 내부 상태(멤버 변수)를 갖고 있거나 다른 private 메서드를 사용하는 복잡한 로직을 갖고 있을 때
2. 다른 인퍼페이스와 의존관계를 맺어야 할 때

물론 100% 결론내린 건 아니다. 지금의 내 기준이다.

<br/>

### stream으로 필터링하기

```java
public Double[] getHighResultsThan(double num) {
  return resultDeque.stream()
    .filter(result -> result > num)
    .toArray(Double[]::new);
}
```

입력 값보다 더 큰 결과들을 반환하는 함수를 위와 같이 작성했다.

<br/>

함수형 프로그래밍은 확실이... '어떻게'보다 '무엇'에 집중한게 보이는 선언적 프로그래밍 방식이다.

위의 코드만 봐도 아 'resultDeque에서 필터링해서 배열로 바꿨구나' 가 보이지 않는가

<br/>

하지만 남발하다가는 성능 이슈가 있을 수 있으니, 고려해서 사용해야지

<br/>

### 실수를 검증하는 정규표현식

실수를 검증하기 위해서 표현식을 변경했다.

추가적으로 앞에 -, + 기호도 허용했다.

```java
double number Double.parseDouble(scanner.next("[-+]?[0-9]*\\.?[0-9]+"));
```

순서대로 읽어보면 -나 +는 있거나 없어도 되고(`?`),

0-9가 0개 이상 있어야 하며(`*`),

점 이 있거나 없어도 되고(`?`),

0-9가 1개 이상 있어야 한다(`*`)

<br/>

이렇게 반복/조건 메타 문자만 잘 활용해도 코드 몇 줄이 줄어든다! 얼마나 좋아!

<br/>

## 아직 해결 못한 점

- 반환해야할 자료형이 확정이 된 경우에도 제네릭을 쓰는게 이점이 있는가
  - '어떤 입력 타입을 넣었는지 모르니, 어떤 반환 타입이 나올지 몰라서 이때 제네릭을 쓴다' 라는 내 기준이 맞을지
- enum 내 스타일 처럼 쓰기도 하는지... 아는 거 다 동원해서 원하는 대로 만들긴 했는데

<br/>

## 마치며

객체지향은 역시 어렵다.

원래 내일부터 Java Swing 을 통해 GUI로 입력받는게 재미있을 것 같아서 이 아이디어로 Level 4를 진행해보려 했는데,

지금 이 상태에서 Java Swing 하나하나 학습해가면서 Pannel꾸미고 이벤트 처리하고.. 이런 고민을 하는 것보다는,

POJO 기반으로 어떻게 더 좋은 코드를 작성할 수 있을지 고민하는게 학습에 도움이 될 것 같다.

당분간 기능 추가같은 욕심은 버리고 기초부터 쌓자.