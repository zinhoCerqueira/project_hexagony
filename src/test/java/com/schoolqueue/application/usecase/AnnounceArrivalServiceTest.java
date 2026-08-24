package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase.AnnounceArrivalCommand;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
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
class AnnounceArrivalServiceTest {

  @Mock QueueRepositoryPort queueRepositoryPort;

  @Mock QueueNotificationPort notificationPort;

  private AnnounceArrivalCommand newCommand(BigDecimal latitude, BigDecimal longitude) {
    return new AnnounceArrivalCommand(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), latitude, longitude, 10);
  }

  @Test
  @DisplayName("saves and notifies a new EN_ROUTE item when no active item exists")
  void shouldSaveAndNotifyEnRouteItemWhenNoActiveItemExists() {
    AnnounceArrivalCommand command =
        newCommand(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(Optional.empty());
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    AnnounceArrivalService service =
        new AnnounceArrivalService(queueRepositoryPort, notificationPort);

    PickupQueueItem result = service.execute(command);

    ArgumentCaptor<PickupQueueItem> captor = ArgumentCaptor.forClass(PickupQueueItem.class);
    verify(queueRepositoryPort).save(captor.capture());

    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(result.called()).isFalse();
    assertThat(captor.getValue().schoolId()).isEqualTo(command.schoolId());
    assertThat(captor.getValue().studentId()).isEqualTo(command.studentId());
    assertThat(captor.getValue().parentId()).isEqualTo(command.parentId());
    assertThat(captor.getValue().estimatedEtaMinutes()).isEqualTo(10);
    assertThat(captor.getValue().currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(captor.getValue().latitude()).isEqualByComparingTo(new BigDecimal("-23.5505"));
    assertThat(captor.getValue().longitude()).isEqualByComparingTo(new BigDecimal("-46.6333"));
    verify(notificationPort).notifyStudentArrivalAnnounced(result);
  }

  @Test
  @DisplayName("throws IllegalStateException when the student already has an active item")
  void shouldThrowIllegalStateExceptionWhenStudentAlreadyHasActiveItem() {
    AnnounceArrivalCommand command =
        newCommand(new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(
            Optional.of(
                new PickupQueueItem(
                    null,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    10,
                    ProximityRange.FAR)));
    AnnounceArrivalService service =
        new AnnounceArrivalService(queueRepositoryPort, notificationPort);

    assertThatThrownBy(() -> service.execute(command))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Já existe um aviso de saída ativo para este aluno.");

    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }

  @Test
  @DisplayName("starts as called when the eta maps to the CLOSE range")
  void shouldStartCalledWhenInitialRangeIsClose() {
    AnnounceArrivalCommand command =
        new AnnounceArrivalCommand(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null, 5);
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(Optional.empty());
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    AnnounceArrivalService service =
        new AnnounceArrivalService(queueRepositoryPort, notificationPort);

    PickupQueueItem result = service.execute(command);

    assertThat(result.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(result.called()).isTrue();
    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("persists null location when the command has no GPS coordinates")
  void shouldPersistNullLocationWhenCommandHasNoGps() {
    AnnounceArrivalCommand command = newCommand(null, null);
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(Optional.empty());
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    AnnounceArrivalService service =
        new AnnounceArrivalService(queueRepositoryPort, notificationPort);

    PickupQueueItem result = service.execute(command);

    assertThat(result.latitude()).isNull();
    assertThat(result.longitude()).isNull();
    verify(queueRepositoryPort).save(any(PickupQueueItem.class));
    verify(notificationPort).notifyStudentArrivalAnnounced(result);
  }
}
