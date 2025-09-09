package com.onclass.bootcamp.domain.api;

import reactor.core.publisher.Flux;
import java.util.List;

public interface CapacityQueryPort {
    Flux<Long> findExistingIdsByIds(List<Long> ids);
}
