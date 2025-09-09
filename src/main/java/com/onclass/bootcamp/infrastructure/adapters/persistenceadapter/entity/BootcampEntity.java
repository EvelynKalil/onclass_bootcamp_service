package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("bootcamps")
public class BootcampEntity {

    @Id
    private Long id;

    private String name;

    private String description;

    @Column("launch_date")
    private LocalDate launchDate;

    private Integer duration;
}
