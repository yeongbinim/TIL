import member.infrastructure.MemberRepository;
import member.infrastructure.MemoryMemberRepository;
import member.service.MemberService;
import member.service.MemberServiceImpl;
import order.infrastructure.MemoryOrderRepository;
import order.infrastructure.OrderRepository;
import order.service.DiscountPolicy;
import order.service.OrderService;
import order.service.OrderServiceImpl;
import order.service.RateDiscountPolicy;

public class AppConfig {
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public OrderService orderService() {
        return new OrderServiceImpl(
                orderRepository(),
                memberRepository(),
                discountPolicy());
    }

    public OrderRepository orderRepository() {
        return new MemoryOrderRepository();
    }

    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }
}