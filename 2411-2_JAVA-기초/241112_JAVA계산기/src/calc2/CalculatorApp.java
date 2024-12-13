package calc2;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import calc2.domain.operation.AddOperation;
import calc2.domain.operation.DevideOperation;
import calc2.domain.operation.MultiplyOperation;
import calc2.domain.operation.Operation;
import calc2.domain.operation.RemainderOperation;
import calc2.domain.operation.SubstractOperation;

public class CalculatorApp {
    private final static DecimalFormat df = new DecimalFormat("0.######");
    private final static Scanner scanner = new Scanner(System.in);
    private final static Calculator<Integer> calculator = new Calculator<>();
    private final static Map<Character, Operation<Integer>> operationMap = new HashMap<>(){{
        put('+', new AddOperation<>());
        put('-', new SubstractOperation<>());
        put('*', new MultiplyOperation<>());
        put('/', new DevideOperation<>());
        put('%', new RemainderOperation<>());
    }};

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
                "명령을 입력하세요 (exit: 종료, calc: 연산, history: 기록, undo: 실행취소): ",
                "잘못된 명령입니다. ['exit', 'calc', 'history', 'undo'] 중 하나를 입력하세요: ",
                "exit|calc|history|undo");

            switch(command) {
                case "calc":
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

                        double result = calculator.calculate(num1, num2, operationMap.get(operator));
                        System.out.printf("== 연산결과 == \n%s\n\n", df.format(result));
                    } catch (ArithmeticException e) {
                        System.out.println("== 0으로 나눌 수 없습니다. 연산 결과가 저장되지 않습니다 ==\n");
                    }
                case "history":
                    Double[] allResult = calculator.getHistory();
                    System.out.printf("== 지금까지의 결과 값들 ==\n%s\n\n", Arrays.toString(allResult));
                    break;
                case "undo":
                    calculator.undo();
                    System.out.println("== 실행취소 되었습니다 ==\n");
                    break;
                case "exit":
                    System.out.println("== 계산기 프로그램을 종료합니다 ==");
                    scanner.close();
                    return;
            }
        }
    }
}
