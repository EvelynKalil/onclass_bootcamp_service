package com.onclass.bootcamp.domain.model;

public record PageRequest(int page, int size) {
    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be 1..100");
    }
    public long offset() { return (long) page * size; }
    public long limit()  { return size; }
}
