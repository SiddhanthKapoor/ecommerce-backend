package com.siddhanth.ecommerce.service;

import com.siddhanth.ecommerce.dto.AddToCartRequest;
import com.siddhanth.ecommerce.dto.CartItemResponse;
import com.siddhanth.ecommerce.model.CartItem;
import com.siddhanth.ecommerce.model.Product;
import com.siddhanth.ecommerce.repository.CartItemRepository;
import com.siddhanth.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    public CartItem addToCart(AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock");
        }
        
        Optional<CartItem> existing = cartItemRepository
                .findByUserIdAndProductId(request.getUserId(), request.getProductId());
        
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartItemRepository.save(item);
        }
        
        CartItem newItem = new CartItem();
        newItem.setUserId(request.getUserId());
        newItem.setProductId(request.getProductId());
        newItem.setQuantity(request.getQuantity());
        return cartItemRepository.save(newItem);
    }
    
    public List<CartItemResponse> getCartByUserId(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream().map(item -> {
            CartItemResponse resp = new CartItemResponse();
            resp.setId(item.getId());
            resp.setProductId(item.getProductId());
            resp.setQuantity(item.getQuantity());
            productRepository.findById(item.getProductId()).ifPresent(resp::setProduct);
            return resp;
        }).collect(Collectors.toList());
    }
    
    public List<CartItem> getCartItemsByUserId(String userId) {
        return cartItemRepository.findByUserId(userId);
    }
    
    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
