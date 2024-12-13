package order.model;

import member.model.Member;

public class Order {
    private Long id;
    private Member member;
    private String itemName;
    private int itemPrice;
    private int discountPrice;

    public Order(Long id, Member member, String itemName, int itemPrice, int discountPrice) {
        this.id = id;
        this.member = member;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }

    public static Order from(Member member, OrderCreate orderCreate, int discountPrice) {
        return new Order(
                null,
                member,
                orderCreate.getItemName(),
                orderCreate.getItemPrice(),
                discountPrice
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }
}
