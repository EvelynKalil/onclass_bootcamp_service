package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository;

import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {

    Mono<Boolean> existsByName(String name);

    // nombre ASC
    @Query("""
      SELECT b.id, b.name, b.description, b.launch_date, b.duration
      FROM bootcamps b
      ORDER BY b.name ASC
      LIMIT :offset, :limit
    """)
    Flux<BootcampEntity> findPageOrderByNameAsc(int offset, int limit);

    // nombre DESC
    @Query("""
      SELECT b.id, b.name, b.description, b.launch_date, b.duration
      FROM bootcamps b
      ORDER BY b.name DESC
      LIMIT :offset, :limit
    """)
    Flux<BootcampEntity> findPageOrderByNameDesc(int offset, int limit);

    // conteo de capacidades ASC, desempate alfabético ASC
    @Query("""
      SELECT b.id, b.name, b.description, b.launch_date, b.duration
      FROM bootcamps b
      LEFT JOIN bootcamp_capacities bc ON bc.bootcamp_id = b.id
      GROUP BY b.id, b.name, b.description, b.launch_date, b.duration
      ORDER BY COUNT(bc.capacity_id) ASC, b.name ASC
      LIMIT :offset, :limit
    """)
    Flux<BootcampEntity> findPageOrderByCapacityCountAsc(int offset, int limit);

    // conteo de capacidades DESC, desempate alfabético ASC
    @Query("""
      SELECT b.id, b.name, b.description, b.launch_date, b.duration
      FROM bootcamps b
      LEFT JOIN bootcamp_capacities bc ON bc.bootcamp_id = b.id
      GROUP BY b.id, b.name, b.description, b.launch_date, b.duration
      ORDER BY COUNT(bc.capacity_id) DESC, b.name ASC
      LIMIT :offset, :limit
    """)
    Flux<BootcampEntity> findPageOrderByCapacityCountDesc(int offset, int limit);

    // total
    @Query("SELECT COUNT(*) FROM bootcamps")
    Mono<Long> countAll();
}
