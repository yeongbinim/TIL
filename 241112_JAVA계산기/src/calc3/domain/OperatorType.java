package calc3.domain;

import java.util.function.BiFunction;

public enum OperatorType {
    ADD('+', (value1, value2) -> value1.doubleValue() + value2.doubleValue()),
    SUBTRACT('-', (value1, value2) -> value1.doubleValue() - value2.doubleValue()),
    MULTIPLY('*', (value1, value2) -> value1.doubleValue() * value2.doubleValue()),
    DIVIDE('/', (value1, value2) -> {
        if (value2.doubleValue() == 0)
            throw new ArithmeticException("0으로 나눌 수 없어");
        return value1.doubleValue() / value2.doubleValue();
    }),
    REMAINDER('%', (value1, value2) -> {
        if (value2.doubleValue() == 0)
            throw new ArithmeticException("0으로 나눌 수 없어");
        return value1.doubleValue() % value2.doubleValue();
    });

    private final BiFunction<Number, Number, Double> expression;
    private final char operator;

    OperatorType(char operator, BiFunction<Number, Number, Double> expression) {
        this.operator = operator;
        this.expression = expression;
    }

    public double operate(Number value1, Number value2) {
        return expression.apply(value1, value2);
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
