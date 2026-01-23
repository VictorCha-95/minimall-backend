package com.minimall.api.product.dto.response;

import java.util.List;

public record ProductSliceResponse<T>(
        List<T> items,
        int page,
        int size,
        boolean hasNext
) {
}
