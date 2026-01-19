package com.siddhanth.ecommerce.service;

import com.siddhanth.ecommerce.dto.CreateOrderRequest;
import com.siddhanth.ecommerce.dto.OrderResponse;
import com.siddhanth.ecommerce.model.*;
import com.siddhanth.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final ProductService productService;
    private final CartService cartService;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<CartItem> cartItems = cartService.getCartItemsByUserId(request.getUserId());
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        double total = 0;
        for (CartItem item : cartItems) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (p.getStock() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for " + p.getName());
            }
            total += p.getPrice() * item.getQuantity();
        }
        
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(total);
        order.setStatus(Order.STATUS_CREATED);
        order.setCreatedAt(Instant.now());
        Order saved = orderRepository.save(order);
        
        List<OrderItem> orderItems = cartItems.stream().map(ci -> {
            Product p = productRepository.findById(ci.getProductId()).get();
            productService.updateStock(ci.getProductId(), ci.getQuantity());
            
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setProductId(ci.getProductId());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice());
            return orderItemRepository.save(oi);
        }).collect(Collectors.toList());
        
        cartService.clearCart(request.getUserId());
        
        OrderResponse resp = new OrderResponse();
        resp.setId(saved.getId());
        resp.setUserId(saved.getUserId());
        resp.setTotalAmount(saved.getTotalAmount());
        resp.setStatus(saved.getStatus());
        resp.setItems(orderItems);
        resp.setCreatedAt(saved.getCreatedAt());
        return resp;
    }
    
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setUserId(order.getUserId());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setStatus(order.getStatus());
        resp.setItems(items);
        resp.setPayment(payment);
        resp.setCreatedAt(order.getCreatedAt());
        return resp;
    }
    
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream().map(order -> {
            OrderResponse resp = new OrderResponse();
            resp.setId(order.getId());
            resp.setUserId(order.getUserId());
            resp.setTotalAmount(order.getTotalAmount());
            resp.setStatus(order.getStatus());
            resp.setItems(orderItemRepository.findByOrderId(order.getId()));
            resp.setPayment(paymentRepository.findByOrderId(order.getId()).orElse(null));
            resp.setCreatedAt(order.getCreatedAt());
            return resp;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (Order.STATUS_PAID.equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel paid order");
        }
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
        
        order.setStatus(Order.STATUS_CANCELLED);
        orderRepository.save(order);
        return getOrderById(orderId);
    }
    
    public void updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }
    
    public Order getOrderEntityById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
