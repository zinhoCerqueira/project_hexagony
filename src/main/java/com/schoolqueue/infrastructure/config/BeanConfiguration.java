package com.schoolqueue.infrastructure.config;

import com.schoolqueue.application.usecase.AnnounceArrivalService;
import com.schoolqueue.application.usecase.FetchActiveQueueService;
import com.schoolqueue.application.usecase.RegisterSchoolService;
import com.schoolqueue.application.usecase.UpdateQueueStatusService;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public RegisterSchoolUseCase registerSchoolUseCase(SchoolRepositoryPort schoolRepositoryPort) {
    return new RegisterSchoolService(schoolRepositoryPort);
  }

  @Bean
  public AnnounceArrivalUseCase announceArrivalUseCase(
      QueueRepositoryPort queueRepositoryPort,
      QueueNotificationPort notificationPort,
      SchoolRepositoryPort schoolRepositoryPort) {
    return new AnnounceArrivalService(
        queueRepositoryPort, notificationPort, schoolRepositoryPort);
  }

  @Bean
  public UpdateQueueStatusUseCase updateQueueStatusUseCase(
      QueueRepositoryPort queueRepositoryPort, QueueNotificationPort notificationPort) {
    return new UpdateQueueStatusService(queueRepositoryPort, notificationPort);
  }

  @Bean
  public FetchActiveQueueUseCase fetchActiveQueueUseCase(
      QueueRepositoryPort queueRepositoryPort) {
    return new FetchActiveQueueService(queueRepositoryPort);
  }
}
