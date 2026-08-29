package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase.UpdateParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateParentServiceTest {

  @Mock ParentRepositoryPort parentRepositoryPort;

  @Test
  @DisplayName("updates an existing parent")
  void shouldUpdateParent() {
    UUID id = UUID.randomUUID();
    Parent existing = new Parent(id, "Old", "111");
    when(parentRepositoryPort.findById(id)).thenReturn(Optional.of(existing));
    when(parentRepositoryPort.save(any(Parent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateParentCommand command = new UpdateParentCommand(id, "New", "222");
    Parent result = new UpdateParentService(parentRepositoryPort).execute(command);

    ArgumentCaptor<Parent> captor = ArgumentCaptor.forClass(Parent.class);
    verify(parentRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("New");
    assertThat(captor.getValue().phone()).isEqualTo("222");
    assertThat(result.name()).isEqualTo("New");
  }

  @Test
  @DisplayName("throws ParentNotFoundException when the parent does not exist")
  void shouldThrowWhenParentDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(parentRepositoryPort.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                new UpdateParentService(parentRepositoryPort)
                    .execute(new UpdateParentCommand(id, "X", "Y")))
        .isInstanceOf(ParentNotFoundException.class);
  }
}
