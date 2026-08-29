package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchParentServiceTest {

  @Mock ParentRepositoryPort parentRepositoryPort;

  @Test
  @DisplayName("returns the parent when it exists")
  void shouldReturnParent() {
    UUID id = UUID.randomUUID();
    Parent parent = new Parent(id, "Maria", "1");
    when(parentRepositoryPort.findById(id)).thenReturn(Optional.of(parent));

    Parent result = new FetchParentService(parentRepositoryPort).execute(id);

    assertThat(result).isEqualTo(parent);
  }
}
