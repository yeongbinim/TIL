package calculator.operation;

public class SubstractOperation <T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        return num1.doubleValue() - num2.doubleValue();
    }
}
