package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacityDTO;
import reactor.core.publisher.Flux;
import java.util.List;

public interface CapacityGatewayPort {
    Flux<Long> findExistingIds(List<Long> ids);
    Flux<CapacityDTO> fetchByIds(List<Long> ids);
}
