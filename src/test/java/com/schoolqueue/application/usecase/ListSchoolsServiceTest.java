package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListSchoolsServiceTest {

  @Mock SchoolRepositoryPort schoolRepositoryPort;

  @Test
  @DisplayName("returns the list of schools from the repository")
  void shouldReturnAllSchools() {
    School a = new School(UUID.randomUUID(), "A", new BigDecimal("0"), new BigDecimal("0"));
    School b = new School(UUID.randomUUID(), "B", new BigDecimal("0"), new BigDecimal("0"));
    when(schoolRepositoryPort.findAll()).thenReturn(List.of(a, b));

    List<School> result = new ListSchoolsService(schoolRepositoryPort).execute();

    assertThat(result).containsExactly(a, b);
  }
}
