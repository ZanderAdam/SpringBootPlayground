package com.springbootplayground.order.fulfillment.service.order;

import java.util.List;

public class NewOrderDTO {
  private Integer userId;
  private List<Integer> orderedProductIds;

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
}
