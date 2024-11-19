package order.infrastructure;

import order.model.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MemoryOrderRepository implements OrderRepository {
    private static final Map<Long, Order> store = new HashMap<>();
    private static long sequence = 0L;

    @Override
    public Order save(Order order) {
        order.setId(++sequence);
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public List<Order> findByMemberId(Long memberId) {
        return store.values().stream()
                .filter(order -> Objects.equals(order.getMember().getId(), memberId))
                .collect(Collectors.toList());
    }
}
