package com.siddhanth.ecommerce.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.siddhanth.ecommerce.dto.CreatePaymentRequest;
import com.siddhanth.ecommerce.dto.PaymentResponse;
import com.siddhanth.ecommerce.model.Payment;
import com.siddhanth.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RazorpayClient razorpayClient;
    
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        com.siddhanth.ecommerce.model.Order order = orderService.getOrderEntityById(request.getOrderId());
        
        if (!com.siddhanth.ecommerce.model.Order.STATUS_CREATED.equals(order.getStatus())) {
            throw new RuntimeException("Order not in CREATED status");
        }
        
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Payment already exists");
        }
        
        try {
            JSONObject orderReq = new JSONObject();
            orderReq.put("amount", (int) (request.getAmount() * 100));
            orderReq.put("currency", "INR");
            orderReq.put("receipt", "order_" + request.getOrderId());
            
            Order razorpayOrder = razorpayClient.orders.create(orderReq);
            String razorpayOrderId = razorpayOrder.get("id");
            
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getAmount());
            payment.setStatus(Payment.STATUS_PENDING);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setCreatedAt(Instant.now());
            Payment saved = paymentRepository.save(payment);
            
            PaymentResponse resp = new PaymentResponse();
            resp.setPaymentId(saved.getId());
            resp.setOrderId(saved.getOrderId());
            resp.setAmount(saved.getAmount());
            resp.setStatus(saved.getStatus());
            resp.setRazorpayOrderId(razorpayOrderId);
            return resp;
            
        } catch (RazorpayException e) {
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }
    
    public void handleWebhook(String payload) {
        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");
        
        if ("payment.captured".equals(event)) {
            JSONObject paymentEntity = json.getJSONObject("payload")
                    .getJSONObject("payment").getJSONObject("entity");
            
            String razorpayOrderId = paymentEntity.getString("order_id");
            String razorpayPaymentId = paymentEntity.getString("id");
            
            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            payment.setStatus(Payment.STATUS_SUCCESS);
            payment.setPaymentId(razorpayPaymentId);
            paymentRepository.save(payment);
            
            orderService.updateOrderStatus(payment.getOrderId(), 
                    com.siddhanth.ecommerce.model.Order.STATUS_PAID);
        } else if ("payment.failed".equals(event)) {
            JSONObject paymentEntity = json.getJSONObject("payload")
                    .getJSONObject("payment").getJSONObject("entity");
            
            String razorpayOrderId = paymentEntity.getString("order_id");
            
            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            payment.setStatus(Payment.STATUS_FAILED);
            paymentRepository.save(payment);
            
            orderService.updateOrderStatus(payment.getOrderId(), 
                    com.siddhanth.ecommerce.model.Order.STATUS_FAILED);
        }
    }
}
