package com.siddhanth.ecommerce.dto;

import com.siddhanth.ecommerce.model.OrderItem;
import com.siddhanth.ecommerce.model.Payment;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private Double totalAmount;
    private String status;
    private List<OrderItem> items;
    private Payment payment;
    private Instant createdAt;
}
