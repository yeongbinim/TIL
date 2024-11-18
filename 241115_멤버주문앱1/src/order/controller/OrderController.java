package order.controller;

import config.annotation.CommandMapping;
import config.annotation.Controller;
import config.console.ConsoleFormat;
import config.console.ConsoleInput;
import order.model.Order;
import order.model.OrderCreate;
import order.service.OrderService;
import order.service.OrderServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@CommandMapping("/orders")
public class OrderController {
    private final AppConfig appConfig = new AppConfig();
    private final OrderService orderService = appConfig.orderService();

    @CommandMapping(method = "POST")
    public String create() {
        System.out.println("\n==주문 정보 입력==");
        Long id = Long.valueOf(ConsoleInput.input(
                "회원 아이디를 적어주세요: ",
                "잘못된 형식입니다. 다시 입력하세요: ",
                "^[0-9]{1,9}$"));
        String itemName = ConsoleInput.input(
                "물건 이름: ",
                "물건 이름이 올바르지 않습니다: ",
                "^[a-zA-Z가-힣][a-zA-Z0-9가-힣]{2,}$");
        int itemPrice = Integer.parseInt(ConsoleInput.input(
                "물건 가격: ",
                "숫자로만 이루어져야 하며, 10000000000 이하로만 가능합니다: ",
                "^[1-9][0-9]{0,9}$"));

        OrderCreate orderCreate = new OrderCreate(itemName, itemPrice);
        Order order = orderService.createOrder(id, orderCreate);
        return ConsoleFormat.toJson(OrderResponse.from(order));
    }

    @CommandMapping(method = "GET")
    public String getAll() {
        System.out.println("\n==주문 정보 조회==");
        Long id = Long.valueOf(ConsoleInput.input(
                "회원 아이디를 적어주세요: ",
                "잘못된 형식입니다. 다시 입력하세요: ",
                "^[0-9]{1,9}$"));

        List<Order> retrieveOrders = orderService.getOrdersByMemberId(id);
        return ConsoleFormat.toJson(retrieveOrders.stream().map(OrderResponse::from).collect(Collectors.toList()));
    }
}
