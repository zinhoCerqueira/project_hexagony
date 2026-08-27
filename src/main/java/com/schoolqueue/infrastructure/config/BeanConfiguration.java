package com.schoolqueue.infrastructure.config;

import com.schoolqueue.application.usecase.RegisterSchoolService;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public RegisterSchoolUseCase registerSchoolUseCase(SchoolRepositoryPort schoolRepositoryPort) {
    return new RegisterSchoolService(schoolRepositoryPort);
  }
}
