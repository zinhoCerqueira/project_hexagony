package com.schoolqueue.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class BeanConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(BeanConfiguration.class, TestPortsConfig.class);

  @Test
  @DisplayName("registers RegisterSchoolUseCase as a Spring bean")
  void shouldExposeRegisterSchoolUseCaseAsBean() {
    contextRunner.run(
        ctx -> {
          assertThat(ctx).hasSingleBean(RegisterSchoolUseCase.class);
          assertThat(ctx.getBean(RegisterSchoolUseCase.class)).isNotNull();
        });
  }

  @Test
  @DisplayName("registers AnnounceArrivalUseCase as a Spring bean")
  void shouldExposeAnnounceArrivalUseCaseAsBean() {
    contextRunner.run(
        ctx -> {
          assertThat(ctx).hasSingleBean(AnnounceArrivalUseCase.class);
          assertThat(ctx.getBean(AnnounceArrivalUseCase.class)).isNotNull();
        });
  }

  @Test
  @DisplayName("registers UpdateQueueStatusUseCase as a Spring bean")
  void shouldExposeUpdateQueueStatusUseCaseAsBean() {
    contextRunner.run(
        ctx -> {
          assertThat(ctx).hasSingleBean(UpdateQueueStatusUseCase.class);
          assertThat(ctx.getBean(UpdateQueueStatusUseCase.class)).isNotNull();
        });
  }

  @Test
  @DisplayName("registers FetchActiveQueueUseCase as a Spring bean")
  void shouldExposeFetchActiveQueueUseCaseAsBean() {
    contextRunner.run(
        ctx -> {
          assertThat(ctx).hasSingleBean(FetchActiveQueueUseCase.class);
          assertThat(ctx.getBean(FetchActiveQueueUseCase.class)).isNotNull();
        });
  }

  @Configuration
  static class TestPortsConfig {

    @Bean
    QueueRepositoryPort queueRepositoryPort() {
      return org.mockito.Mockito.mock(QueueRepositoryPort.class);
    }

    @Bean
    QueueNotificationPort queueNotificationPort() {
      return org.mockito.Mockito.mock(QueueNotificationPort.class);
    }

    @Bean
    SchoolRepositoryPort schoolRepositoryPort() {
      return org.mockito.Mockito.mock(SchoolRepositoryPort.class);
    }

    @Bean
    ClassroomRepositoryPort classroomRepositoryPort() {
      return org.mockito.Mockito.mock(ClassroomRepositoryPort.class);
    }
  }
}
