package calculator.operation;

public class RemainderOperation<T extends Number> implements Operation<T> {
    @Override
    public double operate(T num1, T num2) {
        if (num2.doubleValue() == 0) {
            throw new ArithmeticException("0으로 나눌 수 없습니다.");
        }
        return num1.doubleValue() % num2.doubleValue();
    }
}
