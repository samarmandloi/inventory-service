package com.pm.inventoryservice.dto.responseDto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages
) {
}