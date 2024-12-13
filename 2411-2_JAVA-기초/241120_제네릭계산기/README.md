# JAVA 제네릭 계산기 최종

멘토님 세션을 듣고, type을 매개변수로 받아서 해당 타입과 같다면 각각 타입에 맞게 결과를 반환해줄 수 있다는 걸 알았다.

```java
N typedResult;
if (type == Integer.Class) {
  typedResult = (N) Integer.valueOf(result.intValue());
} else if ....
```

이것을 통해 계속 고민하던 '**매개변수의 타입이 반환 타입을 결정할 수 있을 때 제네릭이 쓸모가 있다**' 의 조건을 충족시키도록 반환 타입도 지정할 방법을 알아냈다.

<br/>

### 목차

- [Type 별로 직접 처리하기](#type-별로-직접-처리하기)
- [타입 정보는 한번으로 족하다](#타입-정보는-한번으로-족하다)
- [Operator 분리](#operator-분리)


<br/>

### Type 별로 직접 처리하기

```java
public class Calculator<N extends Number> {
  List<N> results = new ArrayList<>();
  Class<N> type;
  public Calculator(Class<N> type) {
    this.type = type;
  }

  public N calculate(N num1, N num2, char operator) {
    Number result = switch (operator) {
      case '+' -> num1.doubleValue() + num2.doubleValue();
      case '-' -> num1.doubleValue() - num2.doubleValue();
      case '*' -> num1.doubleValue() * num2.doubleValue();
      case '/' -> num1.doubleValue() / num2.doubleValue();
      case '%' -> num1.doubleValue() % num2.doubleValue();
      default -> 0.0;
    };

    N typedResult = switch (type.getSimpleName()) {
      case "Integer" -> (N) Integer.valueOf(result.intValue());
      case "Double" -> (N) Double.valueOf(result.doubleValue());
      case "Long" -> (N) Long.valueOf(result.longValue());
      case "Float" -> (N) Float.valueOf(result.floatValue());
      case "Byte" -> (N) Byte.valueOf(result.byteValue());
      case "Short" -> (N) Short.valueOf(result.shortValue());
      default -> throw new IllegalStateException("지원하지 않는 타입");
    };

    results.add(typedResult);
    return typedResult;
  }
}
```

다음과 같이 작성하면, 타입전용 계산기가 해당 타입만 받아들이고 해당 타입만 저장해서 해당 타입만 반환한다.

원래는 `type == Integer.Class` 로 비교해야 하지만, switch문을 쓰기 위해 type.getSimpleName()으로 했다.

살짝 노가다 같긴 한데, 그래도 Number형은 저 6개 밖에 없으니 괜찮을 것이다.

```java
Calculator<Integer> integerCalculator = new Calculator<>(Integer.Class)
```

이런 식으로 사용할 수 있다.

<br/>

### 타입 정보는 한번으로 족하다

저 `Integer.Class` 를 넘긴다는 것 자체가 너무 거슬린다.

제네릭으로 분명히 "이건 Integer 전용이야!"라고 말했는데, 굳이굳이 `Integer.Class`를 한 번 더 알려줘야 된다는게 조금 아쉬웠다.

저 type을 받는 이유는 런타임 중에 현재 인스턴스가 어떤 타입인지 확인하여 그에 맞게 `Integer.valueOf()` 이런걸로 변환시켜주기 위해서인데,

제네릭은 런타임에 올라가면 사라져 버리기 때문이다.



그런데 런타임에 현재 어떤 타입의 Calculator 인스턴스인지 확인시켜줄 수 있는 게 calculate함수에 있었다!

```java
public N calculate(N num1, N num2, char operator){}
```

바로 저 N num1, N num2 이다.



지금은 아직 N이라는 제네릭 타입이지만, 프로그램이 실행될 때에는 저기에 인스턴스가 들어올 것이다.

그러면 생성자를 통해 받은 type을 비교하는 대신, 저 num1과 num2의 인스턴스가 Integer인지 Double인지 확인만 하면 되는 것이다.

```java
public class Calculator<N extends Number> {
  List<N> results = new ArrayList<>();

  public N calculate(N num1, N num2, char operator) {
  ...(생략)
  N typedResult;
  if (num1 instanceof Double && num2 instanceof Double) {
    typedResult = (N) Double.valueOf(result.doubleValue());
  } else if (num1 instanceof Float && num2 instanceof Float) {
    typedResult = (N) Float.valueOf(result.floatValue());
  } else if (num1 instanceof Long && num2 instanceof Long) {
    typedResult = (N) Long.valueOf(result.longValue());
  } else if (num1 instanceof Integer && num2 instanceof Integer) {
    typedResult = (N) Integer.valueOf(result.intValue());
  } else if (num1 instanceof Short && num2 instanceof Short) {
    typedResult = (N) Short.valueOf(result.shortValue());
  } else if (num1 instanceof Byte && num2 instanceof Byte) {
    typedResult = (N) Byte.valueOf(result.byteValue());
  } else {
    throw new IllegalStateException("지원하지 않는 타입");
  }

  results.add(typedResult);
  return typedResult;
}
```

위와 같이 코드를 바꿨다.

제네릭 때문에 실제로 num1과 num2에 다른 타입이 들어오는 경우는 없을테니 &&은 제거해서 왼쪽만 남겼다.



```java
Calculator<Integer> integerCalculator = new Calculator<>();
Calculator<Double> doubleCalculator = new Calculator<>();
Calculator<Float> floatCalculator = new Calculator<>();
```

이런식으로 깔끔하게 사용할 수 있다.

<br/>

### Operator 분리

이 제네릭 적용에서 정말 많이 헤맸는데, 이유를 생각해보면 너무 꼬아서 생각한게 문제였던 것 같다.

_'Integer계산기는 `Integer`만 입력 받아서, `Integer`로만 처리해야 되고, `Integer`만 반환해야돼'_

하지만 결국 Calculator의 진정한 책임은 연산이 아니라, 저 결과들을 잘 저장하고 반환하는 것이다.

그렇다면 연산에 대한 책임은 누가 가져야 하는가?

그 연산의 책임을 갖는 Operator를 분리하였다.

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

이렇게 연산의 책임을 가진 Enum으로 분리하였는데, Enum을 사용한 이유는 묶어주기 위해서였다.

저 '+'와 `(value1, value2) -> value1 + value2` 이 연산이 관련이 있다! 라고 묶어주기 위해서였다.

아직 풀리지 않은 의문은 저런 방식을 많이 사용하느냐.. 이다.

여튼 Calculator 클래스로부터 OperatorType을 완벽히 분리하였고, 변경된 calculate메서드는 다음과 같다.

```java
public class Calculator<N extends Number> {
  List<N> results = new ArrayList<>();

  public N calculate(N num1, N num2, OperatorType operatorType) {
  Number result = operatorType.operate(num1, num2);
  N typedResult;
  if (num1 instanceof Double && num2 instanceof Double) {
    typedResult = (N) Double.valueOf(result.doubleValue());
  } else if (num1 instanceof Float && num2 instanceof Float) {
    typedResult = (N) Float.valueOf(result.floatValue());
  } else if (num1 instanceof Long && num2 instanceof Long) {
    typedResult = (N) Long.valueOf(result.longValue());
  } else if (num1 instanceof Integer && num2 instanceof Integer) {
    typedResult = (N) Integer.valueOf(result.intValue());
  } else if (num1 instanceof Short && num2 instanceof Short) {
    typedResult = (N) Short.valueOf(result.shortValue());
  } else if (num1 instanceof Byte && num2 instanceof Byte) {
    typedResult = (N) Byte.valueOf(result.byteValue());
  } else {
    throw new IllegalStateException("지원하지 않는 타입");
  }
  results.add(typedResult);
  return typedResult;
}
```

