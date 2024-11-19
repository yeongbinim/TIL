package config.console;

import java.util.Scanner;

public class ConsoleInput {
    private final static Scanner scanner = new Scanner(System.in);

    /**
     * 사용자 입력을 받아 정규식 패턴과 일치하는지 확인하고 반환하는 메서드입니다.
     *
     * @param prompt 사용자에게 보여질 입력 요청 메시지
     * @param wrongPrompt 입력 패턴과 일치하지 않을 때 보여질 메시지
     * @param pattern 입력 받을 값이 일치해야 하는 정규식 패턴
     * @return 사용자로부터 받은 패턴에 일치하는 입력값
     */
    public static String input(String prompt, String wrongPrompt, String pattern) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.next();
            if (input.matches(pattern)) {
                return input;
            } else {
                System.out.print(wrongPrompt);
            }
        }
    }
}
