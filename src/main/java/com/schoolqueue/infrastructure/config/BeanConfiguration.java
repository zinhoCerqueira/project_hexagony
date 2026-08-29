package com.schoolqueue.infrastructure.config;

import com.schoolqueue.application.usecase.AnnounceArrivalService;
import com.schoolqueue.application.usecase.FetchActiveQueueService;
import com.schoolqueue.application.usecase.FetchClassroomService;
import com.schoolqueue.application.usecase.FetchParentService;
import com.schoolqueue.application.usecase.FetchSchoolService;
import com.schoolqueue.application.usecase.FetchStudentService;
import com.schoolqueue.application.usecase.ListClassroomsBySchoolService;
import com.schoolqueue.application.usecase.ListParentsService;
import com.schoolqueue.application.usecase.ListSchoolsService;
import com.schoolqueue.application.usecase.ListStudentsByClassroomService;
import com.schoolqueue.application.usecase.ListStudentsBySchoolService;
import com.schoolqueue.application.usecase.RegisterClassroomService;
import com.schoolqueue.application.usecase.RegisterParentService;
import com.schoolqueue.application.usecase.RegisterSchoolService;
import com.schoolqueue.application.usecase.RegisterStudentService;
import com.schoolqueue.application.usecase.UpdateClassroomService;
import com.schoolqueue.application.usecase.UpdateParentService;
import com.schoolqueue.application.usecase.UpdateQueueStatusService;
import com.schoolqueue.application.usecase.UpdateSchoolService;
import com.schoolqueue.application.usecase.UpdateStudentService;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.FetchClassroomUseCase;
import com.schoolqueue.domain.ports.in.FetchParentUseCase;
import com.schoolqueue.domain.ports.in.FetchSchoolUseCase;
import com.schoolqueue.domain.ports.in.FetchStudentUseCase;
import com.schoolqueue.domain.ports.in.ListClassroomsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.ListParentsUseCase;
import com.schoolqueue.domain.ports.in.ListSchoolsUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsByClassroomUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateStudentUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
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
  public RegisterParentUseCase registerParentUseCase(ParentRepositoryPort parentRepositoryPort) {
    return new RegisterParentService(parentRepositoryPort);
  }

  @Bean
  public FetchParentUseCase fetchParentUseCase(ParentRepositoryPort parentRepositoryPort) {
    return new FetchParentService(parentRepositoryPort);
  }

  @Bean
  public ListParentsUseCase listParentsUseCase(ParentRepositoryPort parentRepositoryPort) {
    return new ListParentsService(parentRepositoryPort);
  }

  @Bean
  public UpdateParentUseCase updateParentUseCase(ParentRepositoryPort parentRepositoryPort) {
    return new UpdateParentService(parentRepositoryPort);
  }

  @Bean
  public RegisterStudentUseCase registerStudentUseCase(
      StudentRepositoryPort studentRepositoryPort,
      SchoolRepositoryPort schoolRepositoryPort,
      ClassroomRepositoryPort classroomRepositoryPort,
      ParentRepositoryPort parentRepositoryPort,
      ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort) {
    return new RegisterStudentService(
        studentRepositoryPort,
        schoolRepositoryPort,
        classroomRepositoryPort,
        parentRepositoryPort,
        parentStudentLinkRepositoryPort);
  }

  @Bean
  public FetchStudentUseCase fetchStudentUseCase(StudentRepositoryPort studentRepositoryPort) {
    return new FetchStudentService(studentRepositoryPort);
  }

  @Bean
  public ListStudentsBySchoolUseCase listStudentsBySchoolUseCase(
      StudentRepositoryPort studentRepositoryPort) {
    return new ListStudentsBySchoolService(studentRepositoryPort);
  }

  @Bean
  public ListStudentsByClassroomUseCase listStudentsByClassroomUseCase(
      StudentRepositoryPort studentRepositoryPort) {
    return new ListStudentsByClassroomService(studentRepositoryPort);
  }

  @Bean
  public UpdateStudentUseCase updateStudentUseCase(
      StudentRepositoryPort studentRepositoryPort,
      SchoolRepositoryPort schoolRepositoryPort,
      ClassroomRepositoryPort classroomRepositoryPort,
      ParentRepositoryPort parentRepositoryPort,
      ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort) {
    return new UpdateStudentService(
        studentRepositoryPort,
        schoolRepositoryPort,
        classroomRepositoryPort,
        parentRepositoryPort,
        parentStudentLinkRepositoryPort);
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
