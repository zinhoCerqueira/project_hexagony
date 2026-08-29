package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase.RegisterParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterParentServiceTest {

  @Mock ParentRepositoryPort parentRepositoryPort;

  @Test
  @DisplayName("saves the parent and returns it with a generated id")
  void shouldSaveParent() {
    when(parentRepositoryPort.save(any(Parent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisterParentCommand command = new RegisterParentCommand("Maria", "11999998888");
    Parent result = new RegisterParentService(parentRepositoryPort).execute(command);

    ArgumentCaptor<Parent> captor = ArgumentCaptor.forClass(Parent.class);
    verify(parentRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().id()).isNotNull();
    assertThat(result.name()).isEqualTo("Maria");
    assertThat(result.phone()).isEqualTo("11999998888");
  }
}
