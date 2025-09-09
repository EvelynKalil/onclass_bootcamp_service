package com.onclass.bootcamp.domain.model;

import java.util.List;

public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> Page<T> of(List<T> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil(total / (double) size);
        return new Page<>(content, page, size, total, totalPages);
    }
}
