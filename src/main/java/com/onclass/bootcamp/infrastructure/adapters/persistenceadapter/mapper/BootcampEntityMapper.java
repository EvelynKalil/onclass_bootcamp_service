package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.mapper;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampEntityMapper {

    @Mapping(source = "id",          target = "id")
    @Mapping(source = "name",        target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "launchDate",  target = "launchDate")
    @Mapping(source = "duration",    target = "duration")
    Bootcamp toModel(BootcampEntity entity);

    BootcampEntity toEntity(Bootcamp model);
}
