package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import lombok.Data;
import java.util.List;

@Data
public class CapacityDTO {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
}
