package com.onclass.bootcamp.infrastructure.adapters.technologyadapter;

import com.onclass.bootcamp.domain.spi.TechnologyGatewayPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.TechnologyDTO;
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
public class TechnologyWebClientAdapter implements TechnologyGatewayPort {

    private final WebClient.Builder builder;

    @Value("${app.technology.base-url}")
    private String baseUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private WebClient client() {
        return builder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Flux.empty();

        return fetchByIds(ids)
                .map(TechnologyDTO::getId)
                .distinct()
                .timeout(TIMEOUT);
    }

    @Override
    public Flux<TechnologyDTO> fetchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Flux.empty();

        WebClient wc = client();
        int concurrency = 5;

        return Flux.fromIterable(distinct(ids))
                .flatMap(id ->
                                wc.get()
                                        .uri(uriBuilder -> uriBuilder.path("/technologies/{id}").build(id))
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<APIResponse<TechnologyDTO>>() {})
                                        .map(APIResponse::getData)
                                        .doOnNext(t -> log.info("Fetched technology {} -> {}", id, t))
                                        .doOnError(ex -> log.error("Error fetching technology {}", id, ex))
                                        .onErrorResume(ex -> Mono.empty()),
                        concurrency
                );
    }

    private static List<Long> distinct(List<Long> ids) {
        Set<Long> set = ids.stream().collect(Collectors.toSet());
        return set.stream().toList();
    }
}
