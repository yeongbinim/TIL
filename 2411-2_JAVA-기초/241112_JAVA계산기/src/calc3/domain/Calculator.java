package calc3.domain;

import java.util.ArrayDeque;

public class Calculator {
    private final ArrayDeque<Double> resultDeque = new ArrayDeque<>();

    public double calculate(Number num1, Number num2, OperatorType operatorType) {
        double result = operatorType.operate(num1, num2);
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

    public Double[] getHighResultsThan(double num) {
        return resultDeque.stream()
            .filter(result -> result > num)
            .toArray(Double[]::new);
    }
}
