package com.schoolqueue.infrastructure.config;

import com.schoolqueue.application.usecase.AnnounceArrivalService;
import com.schoolqueue.application.usecase.FetchActiveQueueService;
import com.schoolqueue.application.usecase.FetchClassroomService;
import com.schoolqueue.application.usecase.FetchSchoolService;
import com.schoolqueue.application.usecase.ListClassroomsBySchoolService;
import com.schoolqueue.application.usecase.ListSchoolsService;
import com.schoolqueue.application.usecase.RegisterClassroomService;
import com.schoolqueue.application.usecase.RegisterSchoolService;
import com.schoolqueue.application.usecase.UpdateClassroomService;
import com.schoolqueue.application.usecase.UpdateQueueStatusService;
import com.schoolqueue.application.usecase.UpdateSchoolService;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.FetchClassroomUseCase;
import com.schoolqueue.domain.ports.in.FetchSchoolUseCase;
import com.schoolqueue.domain.ports.in.ListClassroomsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.ListSchoolsUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
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
  public FetchSchoolUseCase fetchSchoolUseCase(SchoolRepositoryPort schoolRepositoryPort) {
    return new FetchSchoolService(schoolRepositoryPort);
  }

  @Bean
  public ListSchoolsUseCase listSchoolsUseCase(SchoolRepositoryPort schoolRepositoryPort) {
    return new ListSchoolsService(schoolRepositoryPort);
  }

  @Bean
  public UpdateSchoolUseCase updateSchoolUseCase(SchoolRepositoryPort schoolRepositoryPort) {
    return new UpdateSchoolService(schoolRepositoryPort);
  }

  @Bean
  public RegisterClassroomUseCase registerClassroomUseCase(
      SchoolRepositoryPort schoolRepositoryPort,
      ClassroomRepositoryPort classroomRepositoryPort) {
    return new RegisterClassroomService(schoolRepositoryPort, classroomRepositoryPort);
  }

  @Bean
  public FetchClassroomUseCase fetchClassroomUseCase(
      ClassroomRepositoryPort classroomRepositoryPort) {
    return new FetchClassroomService(classroomRepositoryPort);
  }

  @Bean
  public ListClassroomsBySchoolUseCase listClassroomsBySchoolUseCase(
      ClassroomRepositoryPort classroomRepositoryPort) {
    return new ListClassroomsBySchoolService(classroomRepositoryPort);
  }

  @Bean
  public UpdateClassroomUseCase updateClassroomUseCase(
      ClassroomRepositoryPort classroomRepositoryPort, SchoolRepositoryPort schoolRepositoryPort) {
    return new UpdateClassroomService(classroomRepositoryPort, schoolRepositoryPort);
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
