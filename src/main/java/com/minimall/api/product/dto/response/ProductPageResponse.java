package com.minimall.api.product.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record ProductPageResponse(
        List<ProductListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static ProductPageResponse from(Page<com.minimall.domain.product.Product> pageResult) {
        List<ProductListItemResponse> items = pageResult.getContent().stream()
                .map(ProductListItemResponse::from)
                .toList();

        return new ProductPageResponse(
                items,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext()
        );
    }
}
