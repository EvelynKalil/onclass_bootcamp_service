package com.onclass.bootcamp.domain.api;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.model.Page;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampServicePort {
    Mono<Bootcamp> register(Bootcamp bootcamp);
    Flux<Bootcamp> list();
    Mono<Bootcamp> findById(Long id);
    Mono<Page<Bootcamp>> list(PageRequest page, SortBy sortBy, Order order);
}
