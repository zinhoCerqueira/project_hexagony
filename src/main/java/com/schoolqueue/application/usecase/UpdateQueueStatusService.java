package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.QueueItemNotFoundException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsArrived;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsCompleted;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateRange;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UpdateQueueStatusService implements UpdateQueueStatusUseCase {

  private final QueueRepositoryPort queueRepositoryPort;
  private final QueueNotificationPort notificationPort;

  public UpdateQueueStatusService(
      QueueRepositoryPort queueRepositoryPort, QueueNotificationPort notificationPort) {
    this.queueRepositoryPort = queueRepositoryPort;
    this.notificationPort = notificationPort;
  }

  @Override
  public PickupQueueItem execute(UpdateQueueStatusCommand command) {
    PickupQueueItem item =
        queueRepositoryPort
            .findById(command.queueItemId())
            .orElseThrow(() -> new QueueItemNotFoundException("Item da fila não encontrado"));

    QueueStatus previousStatus = item.journeyStatus();

    switch (command.action()) {
      case UpdateRange(ProximityRange newRange) -> item.updateRange(newRange);
      case MarkAsArrived ignored -> item.markAsArrived();
      case MarkAsCompleted ignored -> item.markAsCompleted();
      case Cancel ignored -> item.cancel();
    }

    PickupQueueItem saved = queueRepositoryPort.save(item);
    notificationPort.notifyStatusChanged(saved, previousStatus);

    return saved;
  }
}
