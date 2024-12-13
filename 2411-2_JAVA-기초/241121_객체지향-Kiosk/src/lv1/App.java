package lv1;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        List<String> menuItemList = List.of(
            String.format("%-20s | W %-4s | %s", "ShackBurger", 6.9, "토마토, 양상추, 쉑소스가 토핑된 치즈버거"),
            String.format("%-20s | W %-4s | %s", "SmokeShack",  8.9, "베이컨, 체리 페퍼에 쉑소스가 토핑된 치즈버거"),
            String.format("%-20s | W %-4s | %s", "Cheeseburger",6.9, "포테이토 번과 비프패티, 치즈가 토핑된 치즈버거"),
            String.format("%-20s | W %-4s | %s",  "Hamburger",   5.4, "비프패티를 기반으로 야채가 들어간 기본버거"),
            String.format("%-20s | W %-4s | %s", "Lemonade", 2.5, "상큼한 레모네이드"),
            String.format("%-20s | W %-4s | %s", "ShackMeister Ale", 5.9, "독점 제조 맥주"),
            String.format("%-20s | W %-4s | %s", "Fifty/Fifty",  2.5, "아이스티와 레모네이드의 조합"),
            String.format("%-20s | W %-4s | %s", "Root Beer", 2.3, "전통 루트 비어"),
            String.format("%-20s | W %-4s | %s", "Shack Attack", 5.6, "초콜릿 커스터드 위에 페퍼민트, 초콜릿 칩이 토핑된 디저트"),
            String.format("%-20s | W %-4s | %s", "Cheese Fries", 3.9, "치즈와 베이컨이 토핑된 감자튀김"),
            String.format("%-20s | W %-4s | %s", "Concrete Jungle", 6.4, "바나나, 피넛 버터와 함께 블렌드된 커스터드"),
            String.format("%-20s | W %-4s | %s", "Vanilla Shake", 4.2, "고전적인 바닐라 쉐이크")
        );

        Scanner scanner = new Scanner(System.in);
        while (true) {
            AtomicInteger i = new AtomicInteger(1);
            System.out.println(menuItemList.stream()
                    .map(str -> String.format("%2d. %s", i.getAndIncrement(), str))
                    .collect(Collectors.joining("\n")));

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
}
