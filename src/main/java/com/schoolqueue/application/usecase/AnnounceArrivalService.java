package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;

public class AnnounceArrivalService implements AnnounceArrivalUseCase {

  private final QueueRepositoryPort queueRepositoryPort;
  private final QueueNotificationPort notificationPort;

  public AnnounceArrivalService(
      QueueRepositoryPort queueRepositoryPort, QueueNotificationPort notificationPort) {
    this.queueRepositoryPort = queueRepositoryPort;
    this.notificationPort = notificationPort;
  }

  @Override
  public PickupQueueItem execute(AnnounceArrivalCommand command) {
    queueRepositoryPort
        .findActiveByStudentId(command.studentId())
        .ifPresent(
            existing -> {
              throw new IllegalStateException("Já existe um aviso de saída ativo para este aluno.");
            });

    ProximityRange initialRange = ProximityRange.fromEtaMinutes(command.etaMinutes());

    PickupQueueItem newItem =
        new PickupQueueItem(
            null,
            command.schoolId(),
            command.studentId(),
            command.parentId(),
            command.etaMinutes(),
            initialRange);

    if (command.latitude() != null && command.longitude() != null) {
      newItem.updateLocation(command.latitude(), command.longitude());
    }

    PickupQueueItem saved = queueRepositoryPort.save(newItem);
    notificationPort.notifyStudentArrivalAnnounced(saved);

    return saved;
  }
}
