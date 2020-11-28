package com.springbootplayground.order.service.order;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
  private Integer userId;
  private List<Integer> orderedProductIds;
  private LocalDateTime timeStamp;

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public List<Integer> getOrderedProductIds() {
    return orderedProductIds;
  }

  public void setOrderedProductIds(List<Integer> orderedProductIds) {
    this.orderedProductIds = orderedProductIds;
  }

  public LocalDateTime getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(LocalDateTime timeStamp) {
    this.timeStamp = timeStamp;
  }
}
