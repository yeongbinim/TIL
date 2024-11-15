package order.service;

import order.model.Order;
import order.model.OrderCreate;

import java.util.List;

public interface OrderService {
    Order createOrder(Long memberId, OrderCreate orderCreate);

    List<Order> getOrdersByMemberId(Long memberId);
}
