import config.annotation.Bean;
import member.controller.MemberController;
import member.infrastructure.MemberRepository;
import member.infrastructure.MemoryMemberRepository;
import member.service.MemberService;
import member.service.MemberServiceImpl;
import order.controller.OrderController;
import order.infrastructure.MemoryOrderRepository;
import order.infrastructure.OrderRepository;
import order.service.DiscountPolicy;
import order.service.OrderService;
import order.service.OrderServiceImpl;
import order.service.RateDiscountPolicy;


public class AppConfig {
    @Bean
    public MemberController memberController() {
        return new MemberController(memberService());
    }

    @Bean
    public OrderController orderController() {
        return new OrderController(orderService());
    }

    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        System.out.println("repository 생성");
        return new MemoryMemberRepository();
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(
                orderRepository(),
                memberRepository(),
                discountPolicy());
    }

    @Bean
    public OrderRepository orderRepository() {
        return new MemoryOrderRepository();
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }
}
