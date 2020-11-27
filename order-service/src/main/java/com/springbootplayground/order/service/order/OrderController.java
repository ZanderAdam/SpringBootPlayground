package com.springbootplayground.order.service.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
  @GetMapping("/order")
  public String greeting() {
    return "Hello World!";
  }
}
