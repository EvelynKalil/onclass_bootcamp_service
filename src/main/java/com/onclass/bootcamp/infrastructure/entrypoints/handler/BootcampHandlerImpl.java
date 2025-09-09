package com.onclass.bootcamp.infrastructure.entrypoints.handler;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.PageRequest;
import com.onclass.bootcamp.domain.model.SortBy;
import com.onclass.bootcamp.domain.model.Order;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.CapacityGatewayPort;
import com.onclass.bootcamp.domain.spi.TechnologyGatewayPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.*;
import com.onclass.bootcamp.infrastructure.entrypoints.mapper.BootcampMapper;
import com.onclass.bootcamp.infrastructure.entrypoints.util.APIResponse;
import com.onclass.bootcamp.infrastructure.entrypoints.util.ErrorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampHandlerImpl {

    private static final String X_MESSAGE_ID = "X_MESSAGE_ID";

    private final BootcampServicePort service;
    private final BootcampMapper mapper;
    private final CapacityGatewayPort capacityGatewayPort;
    private final TechnologyGatewayPort technologyGatewayPort;

    public Mono<ServerResponse> create(ServerRequest request) {
        final String messageId = request.headers().firstHeader(X_MESSAGE_ID);
        log.info("[{}] POST /bootcamps", messageId);

        return request.bodyToMono(BootcampDTO.class)
                .map(mapper::dtoToDomain)
                .flatMap(service::register)
                .map(mapper::toDto)
                .flatMap(dto -> {
                    APIResponse<BootcampDTO> body = APIResponse.<BootcampDTO>builder()
                            .code(String.valueOf(HttpStatus.CREATED.value()))
                            .message("Created")
                            .identifier(messageId)
                            .date(nowIso())
                            .data(dto)
                            .build();
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body);
                })
                .onErrorResume(ex -> handleError(ex, messageId));
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        final String messageId = request.headers().firstHeader(X_MESSAGE_ID);
        log.info("[{}] GET /bootcamps", messageId);

        int page = parseInt(request.queryParam("page").orElse("0"), 0);
        int size = parseInt(request.queryParam("size").orElse("10"), 10);
        var sortBy = request.queryParam("sortBy")
                .map(String::toLowerCase)
                .map(v -> v.equals("capacitycount") ? SortBy.CAPACITY_COUNT : SortBy.NAME)
                .orElse(SortBy.NAME);
        var order = request.queryParam("order")
                .map(String::toLowerCase)
                .map(v -> v.equals("desc") ? Order.DESC : Order.ASC)
                .orElse(Order.ASC);

        var pr = new PageRequest(page, size);

        return service.list(pr, sortBy, order)
                .flatMap(pageDomain -> Flux.fromIterable(pageDomain.content())
                        .flatMap(bootcamp ->
                                capacityGatewayPort.fetchByIds(bootcamp.getCapacityIds())
                                        .collectList()
                                        .flatMap(capacities ->
                                                Flux.fromIterable(capacities)
                                                        .flatMap(cap ->
                                                                technologyGatewayPort.fetchByIds(cap.getTechnologyIds())
                                                                        .collectList()
                                                                        .map(techs -> new CapacityListItemDTO(
                                                                                cap.getId(),
                                                                                cap.getName(),
                                                                                cap.getDescription(),
                                                                                techs.size(),
                                                                                techs
                                                                        ))
                                                        )
                                                        .collectList()
                                                        .map(capacityList -> mapper.toListItemDTO(bootcamp, capacityList))
                                        )
                        )
                        .collectList()
                        .map(content -> new PageResponse<>(
                                content,
                                pageDomain.page(),
                                pageDomain.size(),
                                pageDomain.totalElements(),
                                pageDomain.totalPages()
                        ))
                )
                .flatMap(payload -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                )
                .onErrorResume(ex -> handleError(ex, messageId));
    }

    // getById igual que antes ...

    /* ====================== helpers ====================== */
    private static String nowIso() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }

    private Mono<ServerResponse> handleError(Throwable ex, String messageId) {
        if (ex instanceof BusinessException be) {
            TechnicalMessage tm = be.getTechnicalMessage();
            log.warn("[{}] BusinessException: {} - {}", messageId, tm.getCode(), tm.getMessage());
            APIResponse<Void> body = APIResponse.<Void>builder()
                    .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                    .message("Bad Request")
                    .identifier(messageId)
                    .date(nowIso())
                    .errors(List.of(new ErrorDTO(tm.getCode(), tm.getMessage(), tm.getParam())))
                    .build();
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        log.error("[{}] Unexpected error", messageId, ex);
        APIResponse<Void> body = APIResponse.<Void>builder()
                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message("Internal Server Error")
                .identifier(messageId)
                .date(nowIso())
                .errors(List.of(new ErrorDTO("500", "Bootcamp error", null)))
                .build();
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private Mono<ServerResponse> notFound(String messageId, String message, String field) {
        APIResponse<Void> body = APIResponse.<Void>builder()
                .code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .message("Not Found")
                .identifier(messageId)
                .date(nowIso())
                .errors(List.of(new ErrorDTO("404", message, field)))
                .build();
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        final String messageId = request.headers().firstHeader(X_MESSAGE_ID);
        final Long id = Long.valueOf(request.pathVariable("id"));
        log.info("[{}] GET /bootcamps/{}", messageId, id);

        return service.findById(id)
                .map(mapper::toDto)
                .flatMap(dto -> {
                    APIResponse<BootcampDTO> body = APIResponse.<BootcampDTO>builder()
                            .code(String.valueOf(HttpStatus.OK.value()))
                            .message("OK")
                            .identifier(messageId)
                            .date(nowIso())
                            .data(dto)
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body);
                })
                .switchIfEmpty(notFound(messageId, "Bootcamp not found", "id"))
                .onErrorResume(ex -> handleError(ex, messageId));
    }

}
