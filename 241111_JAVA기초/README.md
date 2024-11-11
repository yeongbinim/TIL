# Java 기초

드디어 사랑스러운 Java의 시간이다. 

오늘 강의에서는 Java 문법을 배우고, 객체지향적으로 CLI 프로그램을 작성하는 실습을 했다.



## 요리 레시피 메모장

<details>
<summary>[요구사항 자세히 보기]</summary>
<p>
<h3>입력값</h3>
<ul>
  <li>저장할 자료구조명을 입력합니다. (List / Set / Map)</li>
  <li>내가 좋아하는 요리 제목을 먼저 입력합니다.</li>
  <li>이어서 내가 좋아하는 요리 레시피를 한 문장씩 입력합니다.</li>
  <li>입력을 마쳤으면 마지막에 “끝” 문자를 입력합니다.</li>
</ul>
<h3>출력값</h3>
<ul>
  <li>입력이 종료되면 저장한 자료구조 이름과 요리 제목을 괄호로 감싸서 먼저 출력해 줍니다.</li>
  <li>이어서, 입력한 모든 문장 앞에 번호를 붙여서 입력 순서에 맞게 모두 출력해 줍니다.</li>
</ul>
<h3>예시 입력 :</h3>
<pre>
백종원 돼지고기 김치찌개 만들기
4.5
돼지고기는 핏물을 빼주세요.
잘익은 김치 한포기를 꺼내서 잘라주세요.
냄비에 들기름 적당히 두르고 김치를 넣고 볶아주세요.
다진마늘 한스푼, 설탕 한스푼 넣어주세요.
종이컵으로 물 8컵 부어서 센불에 끓여주세요.
핏물 뺀 돼지고기를 넣어주세요.
된장 반스푼, 양파 반개, 청양고추 한개를 썰어서 넣어주세요.
간장 두스푼반, 새우젓 두스푼, 고춧가루 두스푼반 넣어주세요.
중불로 줄여서 오래 끓여주세요~!!	
마지막에 파 쏭쏭 썰어서 마무리하면 돼요^^
</pre>
<h3>예시 출력:</h3>
<pre>
[ 백종원 돼지고기 김치찌개 만들기 ]
별점 : 4 (80.0%)
1. 돼지고기는 핏물을 빼주세요.
2. 잘익은 김치 한포기를 꺼내서 잘라주세요.
3. 냄비에 들기름 적당히 두르고 김치를 넣고 볶아주세요.
4. 다진마늘 한스푼, 설탕 한스푼 넣어주세요.
5. 종이컵으로 물 8컵 부어서 센불에 끓여주세요.
6. 핏물 뺀 돼지고기를 넣어주세요.
7. 된장 반스푼, 양파 반개, 청양고추 한개를 썰어서 넣어주세요.
8. 간장 두스푼반, 새우젓 두스푼, 고춧가루 두스푼반 넣어주세요.
9. 중불로 줄여서 오래 끓여주세요~!!	
10. 마지막에 파 쏭쏭 썰어서 마무리하면 돼요^^
</pre>
</details>
<br/>

```java
interface RecipeStructure<T> {
    String toString();
    void add(String recipe);
}
```

가장먼저 인터페이스를 만들었다. 사용자가 어떤 자료형을 입력하든, 나는 이 인터페이스를 구현하는 클래스의 add()메서드를 통해 레시피를 저장할 것이다.

```java
private class RecipeMap<T extends Map<Integer, String>> implements RecipeStructure<T>{
    private final T map;
    public RecipeMap(T map) {
        this.map = map;
    }
  	@Override
    public void add(String recipe) {
        map.put(map.size() + 1, recipe);
    }
  	@Override
    public String toString() {/*생략*/}
}
private class RecipeCollection<T extends Collection<String>> implements RecipeStructure<T>{
    private final T collection;
    public RecipeCollection(T collection) {
        this.collection = collection;
    }

    @Override
    public void add(String recipe) {
        collection.add(recipe);
    }
    @Override
    public String toString() {/*생략*/}
}
```

RecipeMap과 RecipeCollection을 따로 구현했다. 따로 구현한 이유는 우선 Collection Framework에서 둘의 최상위 부모가 각각 Collection, Map 이기 때문이다.

<img src="https://github.com/user-attachments/assets/f0e3ea10-56ef-4f9e-bdf1-e668d7bc9eb9" width="400" align="center"/>

따라서 List, Set을 사용자가 사용하길 원한다면 저 RecipeCollection을, Map을 사용하길 원한다면 RecipeMap을 생성하여 사용할 것이다.

```java
public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String structure = br.readLine();
    String title = br.readLine();
    RecipeStructure<?> recipeStructure;
    switch(structure.toUpperCase()) {
        case "SET":
            recipeStructure = new RecipeCollection<>(new HashSet<>());
            break;
        case "LIST":
            recipeStructure = new RecipeCollection<>(new ArrayList<>());
            break;
        case "QUEUE":
            recipeStructure = new RecipeCollection<>(new ArrayDeque<>());
            break;
        case "MAP":
            recipeStructure = new RecipeMap<>(new HashMap<>());
            break;
        default :
            return;
    }
    while (true) {
        String input = br.readLine();
        if (input.equals("끝")){
            System.out.printf("[ %s 으로 저장된 %s ]\n%s", structure, title, recipeStructure);
            return;
        }
        recipeStructure.add(input);
    }
}
```

마지막으로 main함수는 사용자가 어떤 자료형을 입력하느냐에 따라 인스턴스를 생성할때, 각각 Set, List, Queue, Map의 구현체인 HashSet, ArrayList, ArrayDeque, HashMap을 주입시켰다.

일종의 전략패턴이라고 볼 수 있다. 어떤 인스턴스가 생성되든, 나는 recipeStructure인터페이스의 add 함수를 호출할 것이기 때문이다.





## 계산기 만들기

<details>
<summary>[요구사항 자세히 보기]</summary>
<p>
<h3>Step 1 : 더하기, 빼기, 나누기, 곱하기 연산을 수행할 수 있는 Calculator 클래스를 만듭니다.</h3>
<ul>
<li>Calulator 클래스는 연산을 수행하는 반환 타입이 double인 calculate 메서드를 가지고 있습니다.</li>
<li>calculate 메서드는 String 타입의 operator 매개변수를 통해 연산자 매개값을 받습니다.</li>
<li>int 타입의 firstNumber, secondNumber 매개변수를 통해 피연산자 값을 받습니다.</li>
<li>calculate 메서드는 전달받은 피연산자, 연산자를 사용하여 연산을 수행합니다.</li>
<li>힌트) if or switch 즉, 제어문을 통해 연산자의 타입을 확인하고 해당하는 타입의 연산을 수행하고 결과값을 반환합니다.</li>
</ul>
<h3>Step 2 : 나머지 연산자(%)를 수행할 수 있게 Calculator 클래스 내부 코드를 변경합니다.</h3>
<ul>
  <li>힌트) 제어문 else if 에 나머지 연산자(%)를 추가합니다.</li>
</ul>
<h3>Step 3</h3>
<img src="https://github.com/user-attachments/assets/0e71507a-3286-4ddf-968f-c1cf7da2cc5e" width="600" align="center"/>
<ul>
<li>AddOperation(더하기), SubstractOperation(빼기), MultiplyOperation(곱하기), DivideOperation(나누기) 연산 클래스를 만든 후 클래스 간의 관계를 고려하여 Calculator 클래스와 관계를 맺습니다.</li>
<li>관계를 맺은 후 필요하다면 Calculator 클래스의 내부 코드를 변경합니다.. 나머지 연산자(%) 기능은 제외합니다.</li>
<li>힌트) AddOperation, SubstractOperation, MultiplyOperation, DivideOperation 연산 클래스들을 만듭니다.</li>
<li>힌트) 각각의 연산 타입에 맞게 operate 메서드를 구현합니다.</li>
<li>힌트) Calculator 클래스와 포함관계를 맺고 생성자를 통해 각각의 연산 클래스 타입의 필드에 객체를 주입합니다.</li>
<li>힌트) calculate 메서드에서 직접 연산을 하지 않고 주입받은 연산 클래스들의 operate 메서드를 사용하여 연산을 진행합니다.</li>
</ul>
<h3>Step 4</h3>
<img src="https://github.com/user-attachments/assets/ff43185f-4ce1-4b0a-b3b9-c3a4679f301a" width="600" align="center"/>
<ul>
<li>AddOperation(더하기), SubstractOperation(빼기), MultiplyOperation(곱하기), DivideOperation(나누기) 연산 클래스들을 AbstractOperation(추상 클래스)를 사용하여 추상화하고 Calculator 클래스의 내부 코드를 변경합니다.</li>
<li>주의) Calculator의 calculate 메서드의 매개변수가 변경되었습니다.</li>
<li>힌트) AbstractOperation 추상 클래스를 만들고 operate 추상 메서드를 만듭니다.</li>
<li>힌트) AddOperation, SubstractOperation, MultiplyOperation, DivideOperation 클래스들은 AbstractOperation 클래스를 상속받고 각각의 연산 </li>타입에 맞게 operate를 오버라이딩 합니다.
<li>힌트) Calculator 클래스는 4개의 연산 클래스들이 상속받고 있는 AbstractOperation 클래스만을 포함합니다.</li>
<li>힌트) 생성자 혹은 Setter를 사용하여 연산을 수행할 연산 클래스의 객체를 AbstractOperation 클래스 타입의 필드에 주입합니다.(다형성)</li>
<li>힌트) calculate 메서드에서는 더 이상 연산자 타입을 받아 구분할 필요 없이 주입 받은 연산 클래스의 operate 메서드를 통해 바로 연산을 수행합니다.</li>
</ul>
</details>
<br/>

이번 요구사항은 상당히 구체적이라서 거의 그대로 따라갔지만, Operation에 제네릭을 추가했는데 이때 Number 하위 자료형들은 다 가능하도록 했다.

```java
package calculator.operation;

public interface Operation <T extends Number> {
    double operate(T num1, T num2);
}
```

그리고 나서 이 인터페이스를 구현하는 덧셈, 뺄셈, 나눗셈, 곱하기의 구현체를 작성했다. 그 중 AddOperation 만 확인해 보자

```java
public class AddOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        return num1.doubleValue() + num2.doubleValue();
    }
}
```

이제 이 인스턴스에 Double이 들어가던, Integer가 들어가던, Float가 들어가던, 내부적으로는 doubleValue()로 인해 double형 자료형으로 바꿔서 연산이 될 것이다.

```java
public class Calculator {
    private Operation<Number> operation;
    Calculator(Operation<Number> operation) {
        this.operation = operation;
    }

    double calculate(Number num1, Number num2) {
        return this.operation.operate(num1, num2);
    }
}
```

마지막으로 Calculator 클래스는 Operation인터페이스에 의존하도록 했는데 이는 SOLID 원칙중에 하나인 DIP 원칙을 고려한 것이다.

Calculator의 기능이 calculate 메서드 하나밖에 없는데... 이러면 굳이 꼭 Calculator 클래스가 있어야 하나 싶다.

main 함수를 작성하긴 했는데 지금은 일단 주먹구구식으로 작성했기 때문에... 좀 더 정제하고 내일 이어서 작성해야 겠다.

지금으로써는 사용자로부터 연산자를 입력받았을때 관리하는 enum도 있으면 좋을 것 같다는 생각이다.



## 마치며

계산기 프로그램을 만드는데 이렇게까지 각각 연산을 Class로 만들어야 되나? 싶기도 하다.

그냥 이런 enum하나 만들어두면 연산하는데 편할 것 같은데..

```java
import java.util.function.BiFunction;

public enum CalculatorType {
    ADD((value1, value2) -> value1.doubleValue() + value2.doubleValue()),
    SUBTRACT((value1, value2) -> value1.doubleValue() - value2.doubleValue()),
    MULTIPLY((value1, value2) -> value1.doubleValue() * value2.doubleValue()),
    DIVIDE((value1, value2) -> {
        if (value2.doubleValue() == 0)
          throw new ArithmeticException("0으로 나눌 수 없어");
        return value1.doubleValue() / value2.doubleValue();
    });

    private BiFunction<Number, Number, Double> expression;

    CalculatorType(BiFunction<Number, Number, Double> expression) {
        this.expression = expression;
    }

    public double calculate(Number value1, Number value2) {
        return expression.apply(value1, value2);
    }
}
```

이게 더 관리하기 편하지 않나...?

답이 없으니깐 이거 참...

어떤게 어떤 장점이 있을지 누워서 자면서 생각해 봐야겠다.

지금 생각으로는 그냥 객체지향 실습하라고 굳이 이런 과제를 낸거 같다.

원래 나라면 후자로 진행할 것 같긴 하다.
