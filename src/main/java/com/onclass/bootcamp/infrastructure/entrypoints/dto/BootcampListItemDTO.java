package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.time.LocalDate;
import java.util.List;

public record BootcampListItemDTO(
        Long id,
        String name,
        String description,
        LocalDate launchDate,
        Integer duration,
        Integer capacityCount,
        List<CapacityListItemDTO> capacities
) {}
