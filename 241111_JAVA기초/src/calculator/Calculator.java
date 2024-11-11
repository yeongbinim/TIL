import calculator.operation.Operation;

public class Calculator {
    private Operation<Number> operation;
    Calculator(Operation<Number> operation) {
        this.operation = operation;
    }

    double calculate(Number num1, Number num2) {
        return this.operation.operate(num1, num2);
    }
}