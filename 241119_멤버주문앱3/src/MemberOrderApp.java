import config.CommandMappingHandler;
import config.ApplicationContext;

import java.util.Scanner;

public class MemberOrderApp {
    public static void main(String[] args) {
        CommandMappingHandler commandMappingHandler = new CommandMappingHandler(new ApplicationContext(AppConfig.class));
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("호출하고 싶은 메서드를 입력하세요 (종료는 exit): ");
                String command = scanner.nextLine();
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
                commandMappingHandler.execute(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}