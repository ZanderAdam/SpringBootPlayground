package com.springbootplayground.order.service.order;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

  private final OrderSender orderSender;

  public OrderService(OrderSender orderSender) {
    this.orderSender = orderSender;
  }

  public void placeOrder(NewOrderDTO order) {
    Order newOrder = new Order();
    newOrder.setUserId(order.getUserId());
    newOrder.setOrderedProductIds(order.getOrderedProductIds());
    newOrder.setTimeStamp(LocalDateTime.now());

    orderSender.send(newOrder);
  }
}
