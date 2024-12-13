package order.service;

import member.model.MemberGrade;
import member.model.Member;

public class FixDiscountPolicy implements DiscountPolicy {
    private final int discountFixAmount = 1000; //1000원 할인
    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == MemberGrade.VIP){
            return discountFixAmount;
        }
        return 0;
    }
}