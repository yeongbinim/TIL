import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CalculatorApp {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        double result = 0;
        Number num1, num2;
        String op;
        while (true) {
            System.out.print("연산을 입력하세요: ");
            String input = reader.readLine();
            if (input.equals("q")) {
                System.out.println("계산기를 종료합니다.");
                return;
            }
            String[] tokens = input.split(" ");
            if (tokens.length == 2 || tokens.length == 3) {
                if (tokens.length == 2) {
                    num1 = result;
                    op = tokens[0];
                    num2 = Double.parseDouble(tokens[1]);
                }
                else {
                    num1 = Double.parseDouble(tokens[0]);
                    op = tokens[1];
                    num2 = Double.parseDouble(tokens[2]);
                }
                CalculatorType calculatorType = CalculatorType.getCalculatorType(op);
                if (calculatorType == null) {
                    System.out.println("잘못된 연산자입니다.");
                    continue;
                }
                result = calculatorType.calculate(num1, num2);
                System.out.println("연산 결과: " + result);
            }  else {
                System.out.println("잘못된 입력입니다.");
            }

        }
    }
}