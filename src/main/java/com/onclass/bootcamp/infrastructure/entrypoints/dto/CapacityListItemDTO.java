package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.util.List;

public record CapacityListItemDTO(
        Long id,
        String name,
        String description,
        Integer techCount,
        List<TechnologyDTO> technologies
) {}
