package com.onclass.bootcamp.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.onclass.bootcamp.domain.constants.Constants.BOOTCAMP_CAPACITY_IDS;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    // Generic
    INTERNAL_ERROR("500", "Something went wrong, please try again", ""),
    INTERNAL_ERROR_IN_ADAPTERS("PRC501", "Something went wrong in adapters, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS(INVALID_REQUEST.code, "Bad Parameters, please verify data", ""),
    UNSUPPORTED_OPERATION("501", "Method not supported, please try again", ""),

    // Bootcamp (HU04, HU05, HU06)
    BOOTCAMP_CREATED("201", "Bootcamp created successfully", ""),
    BOOTCAMP_ALREADY_EXISTS("400", "Bootcamp name already exists", "name"),
    BOOTCAMP_NAME_REQUIRED("400", "Bootcamp name is required", "name"),
    BOOTCAMP_DESCRIPTION_REQUIRED("400", "Bootcamp description is required", "description"),
    BOOTCAMP_NAME_TOO_LONG("400", "Bootcamp name max length is 50", "name"),
    BOOTCAMP_DESCRIPTION_TOO_LONG("400", "Bootcamp description max length is 255", "description"),
    ADAPTER_RESPONSE_NOT_FOUND("404", "Bootcamp not found", "id"),
    BOOTCAMP_MIN_CAPACITIES("400", "A bootcamp must have at least 1 capacity", BOOTCAMP_CAPACITY_IDS),
    BOOTCAMP_MAX_CAPACITIES("400", "A bootcamp must have at most 4 capacities", BOOTCAMP_CAPACITY_IDS),
    CAPACITY_NOT_FOUND("404", "Some capacity IDs do not exist", BOOTCAMP_CAPACITY_IDS);

    public final String code;
    public final String message;
    public final String param;
}
