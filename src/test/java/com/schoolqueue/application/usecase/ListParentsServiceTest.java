package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListParentsServiceTest {

  @Mock ParentRepositoryPort parentRepositoryPort;

  @Test
  @DisplayName("returns the list of parents")
  void shouldListParents() {
    Parent a = new Parent(UUID.randomUUID(), "A", "1");
    Parent b = new Parent(UUID.randomUUID(), "B", "2");
    when(parentRepositoryPort.findAll()).thenReturn(List.of(a, b));

    List<Parent> result = new ListParentsService(parentRepositoryPort).execute();

    assertThat(result).containsExactly(a, b);
  }
}
