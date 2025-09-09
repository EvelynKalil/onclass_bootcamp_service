package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.TechnologyDTO;
import reactor.core.publisher.Flux;
import java.util.List;

public interface TechnologyGatewayPort {
    Flux<Long> findExistingIds(List<Long> ids);
    Flux<TechnologyDTO> fetchByIds(List<Long> ids);
}
