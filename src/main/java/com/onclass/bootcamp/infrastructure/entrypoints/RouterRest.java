package com.onclass.bootcamp.infrastructure.entrypoints;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.onclass.bootcamp.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Tag(name = "Bootcamps", description = "Operaciones sobre bootcamps")
@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({

            // ====== CREATE ======
            @RouterOperation(
                    path = "/bootcamps",
                    produces = {"application/json"},
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "createBootcamp",
                            summary = "Registrar bootcamp",
                            requestBody = @RequestBody(
                                    description = "Datos del bootcamp a registrar",
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = BootcampDTO.class)
                                    )
                            )
                    )
            ),

            // ====== LIST (paginado + ordenado) ======
            @RouterOperation(
                    path = "/bootcamps",
                    produces = {"application/json"},
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "list",
                    operation = @Operation(
                            operationId = "listBootcamps",
                            summary = "Listar bootcamps (paginado y ordenado)",
                            parameters = {
                                    @Parameter(
                                            name = "page", in = ParameterIn.QUERY,
                                            description = "Número de página (>=0)",
                                            schema = @Schema(type = "integer", defaultValue = "0")
                                    ),
                                    @Parameter(
                                            name = "size", in = ParameterIn.QUERY,
                                            description = "Tamaño de página (1..100)",
                                            schema = @Schema(type = "integer", defaultValue = "10")
                                    ),
                                    @Parameter(
                                            name = "sortBy", in = ParameterIn.QUERY,
                                            description = "Campo para ordenar",
                                            schema = @Schema(
                                                    type = "string",
                                                    allowableValues = {"name", "capacityCount"},
                                                    defaultValue = "name"
                                            )
                                    ),
                                    @Parameter(
                                            name = "order", in = ParameterIn.QUERY,
                                            description = "Dirección de orden",
                                            schema = @Schema(
                                                    type = "string",
                                                    allowableValues = {"asc", "desc"},
                                                    defaultValue = "asc"
                                            )
                                    )
                            }
                    )
            ),

            // ====== GET BY ID ======
            @RouterOperation(
                    path = "/bootcamps/{id}",
                    produces = {"application/json"},
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "getById",
                    operation = @Operation(
                            operationId = "getBootcampById",
                            summary = "Buscar bootcamp por ID",
                            parameters = {
                                    @Parameter(
                                            name = "id", in = ParameterIn.PATH,
                                            required = true,
                                            description = "Identificador del bootcamp",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(BootcampHandlerImpl handler) {
        return route(POST("/bootcamps"), handler::create)
                .andRoute(GET("/bootcamps"), handler::list)
                .andRoute(GET("/bootcamps/{id}"), handler::getById);
    }
}
