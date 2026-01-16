package com.minimall.api.product.dto.response;

import com.minimall.domain.product.Product;
import java.time.LocalDateTime;

public record ProductListItemResponse(
        Long productId,
        String productName,
        Integer productPrice,
        Integer stockQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductListItemResponse from(Product product) {
        return new ProductListItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
