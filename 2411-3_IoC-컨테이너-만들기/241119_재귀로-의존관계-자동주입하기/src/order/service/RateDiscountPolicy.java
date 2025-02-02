package order.service;

import config.annotation.Component;
import member.model.Member;
import member.model.MemberGrade;

@Component
public class RateDiscountPolicy implements DiscountPolicy {
    private final double discountRate = 0.1; //10% 할인
    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == MemberGrade.VIP){
            return (int) Math.round(discountRate * price);
        }
        return 0;
    }
}