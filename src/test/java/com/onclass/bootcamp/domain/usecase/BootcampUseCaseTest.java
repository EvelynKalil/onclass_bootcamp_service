package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Capacity;
import com.onclass.bootcamp.domain.model.Order;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.spi.CapacityPersistencePort;
import com.onclass.bootcamp.domain.spi.TechnologyGatewayPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BootcampUseCaseTest {

    private CapacityPersistencePort persistence;
    private TechnologyGatewayPort technologyGatewayPort;
    private BootcampUseCase useCase;

    @BeforeEach
    void setup() {
        persistence = mock(CapacityPersistencePort.class);
        technologyGatewayPort = mock(TechnologyGatewayPort.class);
        useCase = new BootcampUseCase(persistence, technologyGatewayPort);
    }

    private Capacity sampleCapacity() {
        return new Capacity(
                1L,
                "Backend Developer",
                "Focuses on backend technologies",
                List.of(1L, 2L, 3L)
        );
    }

    @Test
    void register_ok() {
        Capacity c = sampleCapacity();

        when(technologyGatewayPort.findExistingIds(anyList()))
                .thenReturn(Flux.fromIterable(c.getTechnologyIds()));
        when(persistence.existsByName(anyString()))
                .thenReturn(Mono.just(false));
        when(persistence.save(any(Capacity.class)))
                .thenReturn(Mono.just(c));

        StepVerifier.create(useCase.register(c))
                .expectNextMatches(saved -> saved.getName().equals("Backend Developer"))
                .verifyComplete();

        verify(persistence).save(c);
    }

    @Test
    void register_fails_when_name_blank() {
        Capacity c = sampleCapacity();
        c.setName("");

        StepVerifier.create(useCase.register(c))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.CAPACITY_NAME_REQUIRED)
                .verify();
    }

    @Test
    void register_fails_when_less_than_three_techs() {
        Capacity c = sampleCapacity();
        c.setTechnologyIds(List.of(1L, 2L));

        StepVerifier.create(useCase.register(c))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.CAPACITY_MIN_TECHS)
                .verify();
    }

    @Test
    void register_fails_when_duplicate_techs() {
        Capacity c = sampleCapacity();
        c.setTechnologyIds(List.of(1L, 1L, 2L));

        StepVerifier.create(useCase.register(c))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.CAPACITY_DUP_TECHS)
                .verify();
    }

    @Test
    void register_fails_when_tech_not_found() {
        Capacity c = sampleCapacity();

        when(technologyGatewayPort.findExistingIds(anyList()))
                .thenReturn(Flux.just(1L, 2L)); // faltaría el 3
        when(persistence.existsByName(anyString())).thenReturn(Mono.just(false));
        when(persistence.save(any())).thenReturn(Mono.empty());
        StepVerifier.create(useCase.register(c))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    assertEquals(TechnicalMessage.TECHNOLOGY_NOT_FOUND,
                            ((BusinessException) ex).getTechnicalMessage());
                })
                .verify();
    }

    @Test
    void register_fails_when_name_already_exists() {
        Capacity c = sampleCapacity();

        when(technologyGatewayPort.findExistingIds(anyList()))
                .thenReturn(Flux.fromIterable(c.getTechnologyIds()));
        when(persistence.existsByName(anyString()))
                .thenReturn(Mono.just(true));
        when(persistence.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.register(c))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    assertEquals(TechnicalMessage.CAPACITY_ALREADY_EXISTS,
                            ((BusinessException) ex).getTechnicalMessage());
                })
                .verify();
    }

    @Test
    void list_all_ok() {
        when(persistence.findAll()).thenReturn(Flux.just(sampleCapacity()));

        StepVerifier.create(useCase.list())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findById_ok() {
        when(persistence.findById(1L)).thenReturn(Mono.just(sampleCapacity()));

        StepVerifier.create(useCase.findById(1L))
                .expectNextMatches(c -> c.getId() == 1L)
                .verifyComplete();
    }

    @Test
    void list_paged_ok() {
        PageRequest pr = new PageRequest(0, 10);
        when(persistence.findPage(pr, SortBy.NAME, Order.ASC))
                .thenReturn(Flux.just(sampleCapacity()));
        when(persistence.countAll()).thenReturn(Mono.just(1L));

        StepVerifier.create(useCase.list(pr, SortBy.NAME, Order.ASC))
                .expectNextMatches(page -> page.totalElements() == 1 && page.content().size() == 1)
                .verifyComplete();
    }
}
