package calc2;

import java.util.ArrayDeque;
import calc2.domain.operation.Operation;

public class Calculator<T extends Number> {
    private final ArrayDeque<Double> resultDeque = new ArrayDeque<>();

    double calculate(T num1, T num2, Operation<T> operation) {
        double result = operation.operate(num1, num2);
        resultDeque.addLast(result);
        return result;
    }

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
