package com.siddhanth.ecommerce.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private Double amount;
    private String status;
    private String razorpayOrderId;
}
