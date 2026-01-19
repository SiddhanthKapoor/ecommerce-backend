package com.siddhanth.ecommerce.dto;

import com.siddhanth.ecommerce.model.Product;
import lombok.Data;

@Data
public class CartItemResponse {
    private String id;
    private String productId;
    private Integer quantity;
    private Product product;
}
