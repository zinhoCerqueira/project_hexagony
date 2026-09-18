package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase.RegisterParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentSchoolLinkRepositoryPort;
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
class RegisterParentServiceTest {

  @Mock ParentRepositoryPort parentRepositoryPort;
  @Mock SchoolRepositoryPort schoolRepositoryPort;
  @Mock ParentSchoolLinkRepositoryPort parentSchoolLinkRepositoryPort;

  @Test
  @DisplayName("saves the parent and links it to the school")
  void shouldSaveParentAndLinkSchool() {
    UUID schoolId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId))
        .thenReturn(
            Optional.of(
                new School(
                    schoolId,
                    "Escola",
                    new BigDecimal("-23.550520"),
                    new BigDecimal("-46.633308"))));
    when(parentRepositoryPort.save(any(Parent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RegisterParentCommand command =
        new RegisterParentCommand("Maria", "11999998888", "maria@mail.com", schoolId);
    Parent result =
        new RegisterParentService(
                parentRepositoryPort, schoolRepositoryPort, parentSchoolLinkRepositoryPort)
            .execute(command);

    ArgumentCaptor<Parent> captor = ArgumentCaptor.forClass(Parent.class);
    verify(parentRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().id()).isNotNull();
    assertThat(result.name()).isEqualTo("Maria");
    assertThat(result.phone()).isEqualTo("11999998888");
    assertThat(result.email()).isEqualTo("maria@mail.com");
    verify(parentSchoolLinkRepositoryPort).linkSchool(result.id(), schoolId);
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowWhenSchoolDoesNotExist() {
    UUID schoolId = UUID.randomUUID();
    when(schoolRepositoryPort.findById(schoolId)).thenReturn(Optional.empty());

    RegisterParentCommand command =
        new RegisterParentCommand("Maria", "11999998888", "maria@mail.com", schoolId);

    assertThatThrownBy(
            () ->
                new RegisterParentService(
                        parentRepositoryPort, schoolRepositoryPort, parentSchoolLinkRepositoryPort)
                    .execute(command))
        .isInstanceOf(SchoolNotFoundException.class);
  }
}
