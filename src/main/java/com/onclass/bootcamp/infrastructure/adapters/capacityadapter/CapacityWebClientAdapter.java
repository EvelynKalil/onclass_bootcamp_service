package com.onclass.bootcamp.infrastructure.adapters.capacityadapter;

import com.onclass.bootcamp.domain.spi.CapacityGatewayPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacityDTO;
import com.onclass.bootcamp.infrastructure.entrypoints.util.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapacityWebClientAdapter implements CapacityGatewayPort {

    private final WebClient.Builder builder;

    @Value("${app.capacity.base-url}")
    private String baseUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private WebClient client() {
        return builder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Flux.empty();

        return fetchByIds(ids)
                .map(CapacityDTO::getId)
                .distinct()
                .timeout(TIMEOUT);
    }

    @Override
    public Flux<CapacityDTO> fetchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Flux.empty();

        WebClient wc = client();
        int concurrency = 5;

        return Flux.fromIterable(distinct(ids))
                .flatMap(id ->
                                wc.get()
                                        .uri(uriBuilder -> uriBuilder.path("/capacities/{id}").build(id))
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<APIResponse<CapacityDTO>>() {})
                                        .map(APIResponse::getData)
                                        .doOnNext(c -> log.info("Fetched capacity {} -> {}", id, c))
                                        .doOnError(ex -> log.error("Error fetching capacity {}", id, ex))
                                        .map(this::toIdNameOnly)
                                        .onErrorResume(ex -> Mono.empty()),
                        concurrency
                );
    }


    private CapacityDTO toIdNameOnly(CapacityDTO dto) {
        if (dto == null) {
            log.warn("CapacityWebClientAdapter recibió un DTO nulo");
            return new CapacityDTO();
        }
        CapacityDTO out = new CapacityDTO();
        out.setId(dto.getId());
        out.setName(dto.getName());
        out.setDescription(dto.getDescription());
        out.setTechnologyIds(dto.getTechnologyIds());
        return out;
    }



    private static List<Long> distinct(List<Long> ids) {
        Set<Long> set = ids.stream().collect(Collectors.toSet());
        return set.stream().toList();
    }
}
