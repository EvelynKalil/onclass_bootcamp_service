package com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("bootcamp_capacities")
public class BootcampCapacityEntity {

    @Id
    private Long id;

    @Column("bootcamp_id")
    private Long bootcampId;

    @Column("capacity_id")
    private Long capacityId;
}
