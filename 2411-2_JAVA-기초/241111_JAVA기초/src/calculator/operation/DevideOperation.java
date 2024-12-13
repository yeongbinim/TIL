package calculator.operation;

public class DevideOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        if (num2.doubleValue() == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return num1.doubleValue() / num2.doubleValue();
    }
}
