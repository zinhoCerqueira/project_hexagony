package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase.RegisterSchoolCommand;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterSchoolServiceTest {

  @Mock SchoolRepositoryPort schoolRepositoryPort;

  private RegisterSchoolCommand newCommand() {
    return new RegisterSchoolCommand(
        "Escola Municipal", new BigDecimal("-23.550520"), new BigDecimal("-46.633308"));
  }

  @Test
  @DisplayName("saves and returns the school with GPS coordinates")
  void shouldSaveAndReturnSchoolWithGpsCoordinates() {
    RegisterSchoolCommand command = newCommand();
    when(schoolRepositoryPort.save(any(School.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    School result = new RegisterSchoolService(schoolRepositoryPort).execute(command);

    ArgumentCaptor<School> captor = ArgumentCaptor.forClass(School.class);
    verify(schoolRepositoryPort).save(captor.capture());

    assertThat(captor.getValue().id()).isNotNull();
    assertThat(result.id()).isNotNull();
    assertThat(result.name()).isEqualTo("Escola Municipal");
    assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("-23.550520"));
    assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("-46.633308"));
  }
}
