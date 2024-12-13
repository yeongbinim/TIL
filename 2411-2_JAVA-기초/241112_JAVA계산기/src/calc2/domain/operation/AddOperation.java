package calc2.operation;

public class AddOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        return num1.doubleValue() + num2.doubleValue();
    }
}
