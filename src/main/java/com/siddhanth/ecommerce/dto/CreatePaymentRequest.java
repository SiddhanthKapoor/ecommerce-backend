package com.siddhanth.ecommerce.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private String orderId;
    private Double amount;
}
