package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.constants.Constants;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.model.Order;
import com.onclass.bootcamp.domain.model.Page;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacityGatewayPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class BootcampUseCase implements BootcampServicePort {

    private final BootcampPersistencePort persistence;
    private final CapacityGatewayPort capacityGatewayPort;

    public BootcampUseCase(BootcampPersistencePort persistence, CapacityGatewayPort capacityGatewayPort) {
        this.persistence = persistence;
        this.capacityGatewayPort = capacityGatewayPort;
    }

    @Override
    public Mono<Bootcamp> register(Bootcamp bootcamp) {
        return Mono.justOrEmpty(bootcamp)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.INVALID_REQUEST)))
                .doOnNext(this::validateBasics) // valida nombre/descr + longitudes
                .flatMap(b -> {
                    // reglas HU04: existencia en capacity + unicidad de nombre + guardar
                    final List<Long> ids = capacityIdsOrEmpty(b);
                    validateCapacityRules(ids);
                    return verifyAllCapacityIdsExist(ids)
                            .then(ensureNameIsUnique(b.getName()))
                            .then(persistence.save(b));
                });
    }

    @Override
    public Flux<Bootcamp> list() {
        return persistence.findAll();
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return persistence.findById(id);
    }

    @Override
    public Mono<Page<Bootcamp>> list(PageRequest pageRequest, SortBy sortBy, Order order) {
        Mono<Long> totalMono = persistence.countAll();
        return persistence.findPage(pageRequest, sortBy, order)
                .collectList()
                .zipWith(totalMono)
                .map(tuple -> Page.of(
                        tuple.getT1(),              // List<Bootcamp>
                        pageRequest.page(),
                        pageRequest.size(),
                        tuple.getT2()               // totalElements
                ));
    }

    /* ====================== Helpers sincrónicos ====================== */

    private void validateBasics(Bootcamp b) {
        if (b.getName() == null || b.getName().isBlank()) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_NAME_REQUIRED);
        }
        if (b.getName().length() > Constants.BOOTCAMP_NAME_MAX_LENGTH) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_NAME_TOO_LONG);
        }
        if (b.getDescription() == null || b.getDescription().isBlank()) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_DESCRIPTION_REQUIRED);
        }
        if (b.getDescription().length() > Constants.BOOTCAMP_DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_DESCRIPTION_TOO_LONG);
        }
    }

    private List<Long> capacityIdsOrEmpty(Bootcamp b) {
        return Optional.ofNullable(b.getCapacityIds()).orElse(List.of());
    }

    private void validateCapacityRules(List<Long> ids) {
        if (ids.size() < Constants.MIN_CAPACITIES) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_MIN_CAPACITIES);
        }
        if (ids.size() > Constants.MAX_CAPACITIES) {
            throw new BusinessException(TechnicalMessage.BOOTCAMP_MAX_CAPACITIES);
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new BusinessException(TechnicalMessage.INVALID_PARAMETERS);
        }
    }

    /* ====================== Helpers reactivos (I/O) ====================== */

    private Mono<Void> verifyAllCapacityIdsExist(List<Long> ids) {
        return capacityGatewayPort.findExistingIds(ids)
                .collectList()
                .flatMap(found -> (found.size() == ids.size())
                        ? Mono.empty()
                        : Mono.error(new BusinessException(TechnicalMessage.CAPACITY_NOT_FOUND)));
    }

    private Mono<Void> ensureNameIsUnique(String name) {
        return persistence.existsByName(name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_ALREADY_EXISTS))
                        : Mono.empty());
    }
}
