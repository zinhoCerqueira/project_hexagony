package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import com.schoolqueue.domain.exception.QueueItemNotFoundException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.Cancel;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsArrived;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsCompleted;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateQueueStatusCommand;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateRange;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateQueueStatusServiceTest {

  @Mock QueueRepositoryPort queueRepositoryPort;

  @Mock QueueNotificationPort notificationPort;

  private PickupQueueItem newItem(ProximityRange initialRange) {
    return new PickupQueueItem(
        null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10, initialRange);
  }

  @Test
  @DisplayName("updates the range and notifies when action is UpdateRange")
  void shouldUpdateRangeAndNotifyWhenActionIsUpdateRange() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    when(queueRepositoryPort.findById(item.id())).thenReturn(Optional.of(item));
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    PickupQueueItem result =
        service.execute(
            new UpdateQueueStatusCommand(item.id(), new UpdateRange(ProximityRange.MEDIUM)));

    ArgumentCaptor<PickupQueueItem> captor = ArgumentCaptor.forClass(PickupQueueItem.class);
    verify(queueRepositoryPort).save(captor.capture());

    assertThat(result.currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(result.called()).isFalse();
    assertThat(captor.getValue().currentRange()).isEqualTo(ProximityRange.MEDIUM);
    verify(notificationPort).notifyStatusChanged(result, QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("auto-calls the student when the update moves the item to CLOSE")
  void shouldAutoCallStudentWhenUpdateMovesItemToClose() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    when(queueRepositoryPort.findById(item.id())).thenReturn(Optional.of(item));
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    PickupQueueItem result =
        service.execute(
            new UpdateQueueStatusCommand(item.id(), new UpdateRange(ProximityRange.CLOSE)));

    assertThat(result.called()).isTrue();
    verify(notificationPort).notifyStatusChanged(result, QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("marks as arrived and notifies when action is MarkAsArrived")
  void shouldMarkAsArrivedAndNotifyWhenActionIsMarkAsArrived() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    when(queueRepositoryPort.findById(item.id())).thenReturn(Optional.of(item));
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    PickupQueueItem result =
        service.execute(new UpdateQueueStatusCommand(item.id(), new MarkAsArrived()));

    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.ARRIVED);
    verify(notificationPort).notifyStatusChanged(result, QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("marks as completed and notifies when action is MarkAsCompleted on a called item")
  void shouldMarkAsCompletedAndNotifyWhenActionIsMarkAsCompletedOnCalledItem() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);
    when(queueRepositoryPort.findById(item.id())).thenReturn(Optional.of(item));
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    PickupQueueItem result =
        service.execute(new UpdateQueueStatusCommand(item.id(), new MarkAsCompleted()));

    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.COMPLETED);
    verify(notificationPort).notifyStatusChanged(result, QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("cancels and notifies when action is Cancel")
  void shouldCancelAndNotifyWhenActionIsCancel() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    when(queueRepositoryPort.findById(item.id())).thenReturn(Optional.of(item));
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    PickupQueueItem result = service.execute(new UpdateQueueStatusCommand(item.id(), new Cancel()));

    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.CANCELLED);
    verify(notificationPort).notifyStatusChanged(result, QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("throws QueueItemNotFoundException when the queue item does not exist")
  void shouldThrowQueueItemNotFoundExceptionWhenQueueItemDoesNotExist() {
    UUID unknownId = UUID.randomUUID();
    when(queueRepositoryPort.findById(unknownId)).thenReturn(Optional.empty());
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    assertThatThrownBy(
            () -> service.execute(new UpdateQueueStatusCommand(unknownId, new MarkAsArrived())))
        .isInstanceOf(QueueItemNotFoundException.class)
        .hasMessage("Item da fila não encontrado");

    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }

  @Test
  @DisplayName("propagates InvalidQueueStateException without saving or notifying")
  void shouldPropagateInvalidQueueStateExceptionWithoutSavingOrNotifying() {
    PickupQueueItem notCalled = newItem(ProximityRange.FAR);
    when(queueRepositoryPort.findById(notCalled.id())).thenReturn(Optional.of(notCalled));
    UpdateQueueStatusService service =
        new UpdateQueueStatusService(queueRepositoryPort, notificationPort);

    assertThatThrownBy(
            () ->
                service.execute(
                    new UpdateQueueStatusCommand(notCalled.id(), new MarkAsCompleted())))
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Aluno não pode ser entregue sem ter sido chamado");

    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }
}
