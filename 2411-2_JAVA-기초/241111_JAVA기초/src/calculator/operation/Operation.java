package calculator.operation;

public interface Operation <T extends Number> {
    double operate(T num1, T num2);
}
