import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Calculator<N extends Number> {
    private final ArrayDeque<N> resultDeque = new ArrayDeque<>();

    public N calculate(N num1, N num2, OperatorType operatorType) {
        Number result = operatorType.operate(num1, num2);
        N typedResult;
        if (num1 instanceof Double) {
            typedResult = (N) Double.valueOf(result.doubleValue());
        } else if (num1 instanceof Float) {
            typedResult = (N) Float.valueOf(result.floatValue());
        } else if (num1 instanceof Long) {
            typedResult = (N) Long.valueOf(result.longValue());
        } else if (num1 instanceof Integer) {
            typedResult = (N) Integer.valueOf(result.intValue());
        } else if (num1 instanceof Short) {
            typedResult = (N) Short.valueOf(result.shortValue());
        } else if (num1 instanceof Byte) {
            typedResult = (N) Byte.valueOf(result.byteValue());
        } else {
            throw new IllegalStateException("지원하지 않는 타입");
        }
        resultDeque.addLast(typedResult);
        return typedResult;
    }

    public List<N> getHighResultsThan(N num) {
        return resultDeque.stream()
                .filter(result -> result.doubleValue() > num.doubleValue())
                .collect(Collectors.toList());
    }
}
