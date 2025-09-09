package com.onclass.bootcamp.application.config;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacityGatewayPort;
import com.onclass.bootcamp.domain.usecase.BootcampUseCase;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.BootcampPersistenceAdapter;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository.BootcampRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

        private final BootcampRepository bootcampRepository;
        private final BootcampCapacityRepository bootcampCapacityRepository;
        private final BootcampEntityMapper bootcampEntityMapper;

        @Bean
        public BootcampPersistencePort bootcampPersistencePort() {
                return new BootcampPersistenceAdapter(
                        bootcampRepository,
                        bootcampCapacityRepository,
                        bootcampEntityMapper
                );
        }

        @Bean
        public BootcampServicePort bootcampService(
                BootcampPersistencePort bootcampPersistencePort,
                CapacityGatewayPort capacityGatewayPort
        ) {
                return new BootcampUseCase(bootcampPersistencePort, capacityGatewayPort);
        }
}
