package calculator.operation;

public class MultiplyOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        return num1.doubleValue() * num2.doubleValue();
    }
}
