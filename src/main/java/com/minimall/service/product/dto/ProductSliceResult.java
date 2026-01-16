package com.minimall.service.product.dto;

import java.util.List;

public record ProductSliceResult<T>(
        List<T> items,
        int page,
        int size,
        boolean hasNext
) {
}
