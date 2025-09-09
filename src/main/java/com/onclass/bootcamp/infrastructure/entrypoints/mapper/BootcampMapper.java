package com.onclass.bootcamp.infrastructure.entrypoints.mapper;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampListItemDTO;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacityListItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BootcampMapper {

    // DTO → Dominio
    @Mapping(target = "id", ignore = true) // se ignora en creación
    @Mapping(source = "name",        target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "launchDate",  target = "launchDate")
    @Mapping(source = "duration",    target = "duration")
    @Mapping(source = "capacityIds", target = "capacityIds")
    Bootcamp dtoToDomain(BootcampDTO dto);

    // Dominio → DTO
    @Mapping(source = "id",          target = "id")
    @Mapping(source = "name",        target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "launchDate",  target = "launchDate")
    @Mapping(source = "duration",    target = "duration")
    @Mapping(source = "capacityIds", target = "capacityIds")
    BootcampDTO toDto(Bootcamp model);

    // Dominio → ListItemDTO (con capacities completas)
    default BootcampListItemDTO toListItemDTO(Bootcamp bootcamp, List<CapacityListItemDTO> capacities) {
        int capacityCount = capacities != null ? capacities.size() : 0;
        return new BootcampListItemDTO(
                bootcamp.getId(),
                bootcamp.getName(),
                bootcamp.getDescription(),
                bootcamp.getLaunchDate(),
                bootcamp.getDuration(),
                capacityCount,
                capacities != null ? capacities : List.of()
        );
    }

    // Overload: solo bootcamp → capacities vacío
    default BootcampListItemDTO toListItemDTO(Bootcamp bootcamp) {
        return toListItemDTO(bootcamp, List.of());
    }
}
