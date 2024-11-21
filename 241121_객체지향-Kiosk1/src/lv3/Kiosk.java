package lv3;

import java.util.List;
import java.util.Scanner;

public class Kiosk {
    List<MenuItem> menuItemList;

    public Kiosk(MenuItem...menu) {
        menuItemList = List.of(menu);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(formatList(menuItemList));
            System.out.printf("%2d. %s\n\n", 0, "종료");
            System.out.print("메뉴를 입력하세요: ");

            int menuNumber = scanner.nextInt();
            if (menuNumber == 0) {
                break;
            }

            System.out.println(menuItemList.get(menuNumber - 1) + "\n");
        }
        scanner.close();
    }

    private String formatList(List<?> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(String.format("\n%2d. %s", i + 1, list.get(i)));
        }
        return builder.toString();
    }
}
