package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.model.Order;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository.BootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BootcampPersistenceAdapter implements BootcampPersistencePort {

    private final BootcampRepository repository;
    private final BootcampCapacityRepository bcRepository;
    private final BootcampEntityMapper mapper;

    @Override
    public Mono<Long> countAll() {
        return repository.countAll();
    }

    @Override
    public Flux<Bootcamp> findPage(PageRequest page, SortBy sortBy, Order order) {
        int limit  = page.size();
        int offset = (int) page.offset();

        Flux<BootcampEntity> entities;
        if (sortBy == SortBy.CAPACITY_COUNT) {
            entities = (order == Order.ASC)
                    ? repository.findPageOrderByCapacityCountAsc(offset, limit)
                    : repository.findPageOrderByCapacityCountDesc(offset, limit);
        } else {
            entities = (order == Order.ASC)
                    ? repository.findPageOrderByNameAsc(offset, limit)
                    : repository.findPageOrderByNameDesc(offset, limit);
        }
        return entities.map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        BootcampEntity entity = mapper.toEntity(bootcamp);

        return repository.save(entity)
                .flatMap(saved -> {
                    Long bootcampId = saved.getId();
                    List<Long> capacityIds = bootcamp.getCapacityIds() == null ? List.of() : bootcamp.getCapacityIds();

                    Mono<Void> saveLinks = Flux.fromIterable(capacityIds)
                            .flatMap(cid -> bcRepository.save(
                                    BootcampCapacityEntity.builder()
                                            .bootcampId(bootcampId)
                                            .capacityId(cid)
                                            .build()
                            ))
                            .then();

                    return saveLinks.thenReturn(
                            Bootcamp.builder()
                                    .id(saved.getId())
                                    .name(saved.getName())
                                    .description(saved.getDescription())
                                    .launchDate(saved.getLaunchDate())
                                    .duration(saved.getDuration())
                                    .capacityIds(capacityIds)
                                    .build()
                    );
                });
    }

    @Override
    public Flux<Bootcamp> findAll() {
        return repository.findAll()
                .flatMap(entity ->
                        bcRepository.findAllByBootcampId(entity.getId())
                                .map(BootcampCapacityEntity::getCapacityId)
                                .collectList()
                                .map(ids -> Bootcamp.builder()
                                        .id(entity.getId())
                                        .name(entity.getName())
                                        .description(entity.getDescription())
                                        .launchDate(entity.getLaunchDate())
                                        .duration(entity.getDuration())
                                        .capacityIds(ids)
                                        .build()
                                )
                );
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return repository.findById(id)
                .flatMap(entity ->
                        bcRepository.findAllByBootcampId(entity.getId())
                                .map(BootcampCapacityEntity::getCapacityId)
                                .collectList()
                                .map(ids -> Bootcamp.builder()
                                        .id(entity.getId())
                                        .name(entity.getName())
                                        .description(entity.getDescription())
                                        .launchDate(entity.getLaunchDate())
                                        .duration(entity.getDuration())
                                        .capacityIds(ids)
                                        .build()
                                )
                );
    }

    @Override
    public Flux<Long> findCapacityIdsByBootcampId(Long bootcampId) {
        return bcRepository
                .findCapacityIdsByBootcampIds(List.of(bootcampId))
                .filter(row -> row.getBootcampId().equals(bootcampId))
                .map(BootcampCapacityRepository.BootcampCapacityIdRow::getCapacityId);
    }

    @Override
    public Flux<Map.Entry<Long, Long>> findCapacityIdsByBootcampIds(Collection<Long> bootcampIds) {
        if (bootcampIds == null || bootcampIds.isEmpty()) return Flux.empty();

        return bcRepository.findCapacityIdsByBootcampIds(bootcampIds)
                .filter(row -> row.getBootcampId() != null && row.getCapacityId() != null)
                .map(row -> Map.entry(row.getBootcampId(), row.getCapacityId()));
    }
}
