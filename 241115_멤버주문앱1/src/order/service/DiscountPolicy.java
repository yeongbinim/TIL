package order.service;

import member.model.Member;

public interface DiscountPolicy {
    int discount(Member member, int price);
}
