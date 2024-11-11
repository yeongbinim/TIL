package main.temp;
import java.util.function.BiFunction;

public enum CalculatorType {
    ADD("+", (value1, value2) -> value1.doubleValue() + value2.doubleValue()),
    SUBTRACT("-", (value1, value2) -> value1.doubleValue() - value2.doubleValue()),
    MULTIPLY("*", (value1, value2) -> value1.doubleValue() * value2.doubleValue()),
    DIVIDE("/", (value1, value2) -> {
        if (value2.doubleValue() == 0)
            throw new ArithmeticException("0으로 나눌 수 없어");
        return value1.doubleValue() / value2.doubleValue();
    }),
    REMAINDER("%", (value1, value2) -> {
        if (value2.doubleValue() == 0)
            throw new ArithmeticException("0으로 나눌 수 없어");
        return value1.doubleValue() % value2.doubleValue();
    });


    private final BiFunction<Number, Number, Double> expression;
    private final String operator;

    CalculatorType(String operator, BiFunction<Number, Number, Double> expression) {
        this.operator = operator;
        this.expression = expression;
    }

    public double calculate(Number value1, Number value2) {
        return expression.apply(value1, value2);
    }

    public boolean equals(String operator) {
        return this.operator.equals(operator);
    }

    public static CalculatorType getCalculatorType(String operator) {
        for (CalculatorType calculatorType: CalculatorType.values()) {
            if (calculatorType.equals(operator)) {
                return calculatorType;
            }
        }
        return null;
    }
}
