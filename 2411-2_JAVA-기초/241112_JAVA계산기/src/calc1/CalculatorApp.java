package calc1;

import java.text.DecimalFormat;
import java.util.Scanner;

public class CalculatorApp {
    private final static DecimalFormat df = new DecimalFormat("0.######");
    private final static Scanner scanner = new Scanner(System.in);

    /**
     * 두 정수와 연산자를 입력받아 적절한 산술 연산을 수행하는 메서드입니다.
     * 지원되지 않는 연산자가 입력되면 기본값으로 0.0을 반환합니다.
     *
     * @param num1 첫 번째 피연산자
     * @param num2 두 번째 피연산자
     * @param operator 수행할 연산의 종류를 지정하는 문자 ('+', '-', '*', '/', '%')
     * @return 연산의 결과 (지원되지 않는 연산자는 0.0)
     */
    private static double calculate(int num1, int num2, char operator) {
        return switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> (double) num1 / num2;
            case '%' -> num1 % num2;
            default -> 0.0;
        };
    }

    /**
     * 패턴에 맞는 입력을 요청하는 메서드입니다.
     *
     * @param prompt 사용자에게 보여질 입력 요청 메시지
     * @param wrongPrompt 입력 패턴과 일치하지 않을 때 보여질 메시지
     * @param pattern 입력 받을 값이 일치해야 하는 정규식 패턴
     * @return 사용자로부터 받은 패턴에 일치하는 입력값
     */
    private static String input(String prompt, String wrongPrompt, String pattern) {
        System.out.print(prompt);
        while (!scanner.hasNext(pattern)) {
            System.out.print(wrongPrompt);
            scanner.next();
        }
        return scanner.next();
    }

    public static void main(String[] args) {
        while(true) {
            String command = input(
                "명령을 입력하세요 (exit: 종료, calc: 연산): ",
                "잘못된 명령입니다. 'exit' 또는 'calc' 중 하나를 입력하세요: ",
                "exit|calc");

            if (command.equals("calc")) {
                try {
                    int num1 = Integer.parseInt(input(
                        "첫번째 양의 정수를 입력하세요: ",
                        "양의 정수가 아닙니다. 다시 입력해 주세요: ",
                        "[0-9]+"));

                    char operator = input(
                        "연산 기호를 입력하세요(+-*/%): ",
                        "허용된 기호가 아닙니다. 다시 입력해 주세요: ",
                        "[+\\-*/%]").charAt(0);

                    int num2 = Integer.parseInt(input(
                        "두번째 양의 정수를 입력하세요: ",
                        "양의 정수가 아닙니다. 다시 입력해 주세요: ",
                        "[0-9]+"));
                    double result = calculate(num1, num2, operator);
                    System.out.printf("== 연산결과 ==\n%s\n\n", df.format(result));
                } catch (ArithmeticException e) {
                    System.out.println("== 0으로 나눌 수 없습니다. 연산 결과가 저장되지 않습니다 ==\n");
                } catch (Exception e) {
                    System.out.println("== 알 수 없는 오류가 발생했습니다 ==\n" + e.getMessage());
                    break;
                }
            }
            else if (command.equals("exit")) {
                System.out.println("== 계산기 프로그램을 종료합니다 ==");
                break;
            }
        }
        scanner.close();
    }
}
