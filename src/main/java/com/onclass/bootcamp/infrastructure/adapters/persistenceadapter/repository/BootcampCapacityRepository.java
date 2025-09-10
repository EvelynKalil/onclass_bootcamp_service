package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository;

import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface BootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {

    Flux<BootcampCapacityEntity> findAllByBootcampId(Long bootcampId);

    @Query("""
      SELECT bc.bootcamp_id AS bootcampId, bc.capacity_id AS capacityId
      FROM bootcamp_capacities bc
      WHERE bc.bootcamp_id IN (:bootcampIds)
      ORDER BY bc.capacity_id ASC
    """)
    Flux<BootcampCapacityIdRow> findCapacityIdsByBootcampIds(Collection<Long> bootcampIds);


    interface BootcampCapacityIdRow {
        Long getBootcampId();
        Long getCapacityId();
    }
}
