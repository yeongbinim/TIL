package order.service;

import member.infrastructure.MemberRepository;
import member.infrastructure.MemoryMemberRepository;
import member.model.Member;
import order.infrastructure.MemoryOrderRepository;
import order.infrastructure.OrderRepository;
import order.model.Order;
import order.model.OrderCreate;

import java.util.List;
import java.util.NoSuchElementException;

public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final OrderRepository orderRepository = new MemoryOrderRepository();

    @Override
    public Order createOrder(Long memberId, OrderCreate orderCreate) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("No member found with id " + memberId));

        int discountPrice = discountPolicy.discount(member, orderCreate.getItemPrice());

        return orderRepository.save(Order.from(member, orderCreate, discountPrice));
    }

    @Override
    public List<Order> getOrdersByMemberId(Long memberId) {
        return orderRepository.findByMemberId(memberId);
    }
}
