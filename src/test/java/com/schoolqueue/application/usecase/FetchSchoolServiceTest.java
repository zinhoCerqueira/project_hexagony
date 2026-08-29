package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchSchoolServiceTest {

  @Mock SchoolRepositoryPort schoolRepositoryPort;

  @Test
  @DisplayName("returns the school when it exists")
  void shouldReturnSchoolWhenItExists() {
    UUID id = UUID.randomUUID();
    School school = new School(id, "Escola A", new BigDecimal("-23.5"), new BigDecimal("-46.6"));
    when(schoolRepositoryPort.findById(id)).thenReturn(Optional.of(school));

    School result = new FetchSchoolService(schoolRepositoryPort).execute(id);

    assertThat(result).isEqualTo(school);
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(schoolRepositoryPort.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new FetchSchoolService(schoolRepositoryPort).execute(id))
        .isInstanceOf(SchoolNotFoundException.class);
  }
}
