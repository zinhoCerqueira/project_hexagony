package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase.UpdateSchoolCommand;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSchoolServiceTest {

  @Mock SchoolRepositoryPort schoolRepositoryPort;

  @Test
  @DisplayName("updates an existing school and saves it")
  void shouldUpdateExistingSchool() {
    UUID id = UUID.randomUUID();
    School existing =
        new School(id, "Old Name", new BigDecimal("-23.5"), new BigDecimal("-46.6"));
    when(schoolRepositoryPort.findById(id)).thenReturn(Optional.of(existing));
    when(schoolRepositoryPort.save(any(School.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateSchoolCommand command =
        new UpdateSchoolCommand(id, "New Name", new BigDecimal("-22.0"), new BigDecimal("-45.0"));

    School result = new UpdateSchoolService(schoolRepositoryPort).execute(command);

    ArgumentCaptor<School> captor = ArgumentCaptor.forClass(School.class);
    verify(schoolRepositoryPort).save(captor.capture());
    School saved = captor.getValue();

    assertThat(saved.name()).isEqualTo("New Name");
    assertThat(saved.latitude()).isEqualByComparingTo(new BigDecimal("-22.0"));
    assertThat(saved.longitude()).isEqualByComparingTo(new BigDecimal("-45.0"));
    assertThat(result.name()).isEqualTo("New Name");
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(schoolRepositoryPort.findById(id)).thenReturn(Optional.empty());
    UpdateSchoolCommand command =
        new UpdateSchoolCommand(id, "Any", new BigDecimal("0"), new BigDecimal("0"));

    assertThatThrownBy(() -> new UpdateSchoolService(schoolRepositoryPort).execute(command))
        .isInstanceOf(SchoolNotFoundException.class);
  }
}
