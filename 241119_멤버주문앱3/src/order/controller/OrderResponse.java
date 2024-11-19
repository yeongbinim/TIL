package order.controller;

import order.model.Order;

public class OrderResponse {
    private Long id;
    private String memberName;
    private String itemName;
    private int itemPrice;
    private int discountPrice;

    public OrderResponse(Long id, String memberName, String itemName, int itemPrice, int discountPrice) {
        this.id = id;
        this.memberName = memberName;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getMember().getName(), order.getItemName(), order.getItemPrice(), order.getDiscountPrice());
    }
}
