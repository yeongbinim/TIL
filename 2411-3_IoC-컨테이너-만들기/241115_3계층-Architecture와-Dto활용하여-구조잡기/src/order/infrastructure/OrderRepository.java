package order.infrastructure;

import order.model.Order;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findByMemberId(Long memberId);
}
