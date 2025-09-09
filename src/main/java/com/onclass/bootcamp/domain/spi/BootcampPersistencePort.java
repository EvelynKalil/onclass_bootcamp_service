package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface BootcampPersistencePort {
    Mono<Long> countAll();
    Flux<Bootcamp> findPage(PageRequest page, SortBy sortBy, Order order);
    Mono<Boolean> existsByName(String name);
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Flux<Bootcamp> findAll();
    Mono<Bootcamp> findById(Long id);
    Flux<Long> findCapacityIdsByBootcampId(Long bootcampId);
    Flux<Map.Entry<Long, Long>> findCapacityIdsByBootcampIds(Collection<Long> bootcampIds);
}

