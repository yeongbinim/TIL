package order.service;

import member.infrastructure.MemberRepository;
import member.model.Member;
import order.infrastructure.OrderRepository;
import order.model.Order;
import order.model.OrderCreate;

import java.util.List;
import java.util.NoSuchElementException;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    public OrderServiceImpl(OrderRepository orderRepository, MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }

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
