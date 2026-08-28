package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class AnnounceArrivalService implements AnnounceArrivalUseCase {

  private final QueueRepositoryPort queueRepositoryPort;
  private final QueueNotificationPort notificationPort;
  private final SchoolRepositoryPort schoolRepositoryPort;

  public AnnounceArrivalService(
      QueueRepositoryPort queueRepositoryPort,
      QueueNotificationPort notificationPort,
      SchoolRepositoryPort schoolRepositoryPort) {
    this.queueRepositoryPort = queueRepositoryPort;
    this.notificationPort = notificationPort;
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public PickupQueueItem execute(AnnounceArrivalCommand command) {
    queueRepositoryPort
        .findActiveByStudentId(command.studentId())
        .ifPresent(
            existing -> {
              throw new IllegalStateException("Já existe um aviso de saída ativo para este aluno.");
            });

    if (command.latitude() == null || command.longitude() == null) {
      throw new IllegalArgumentException("Latitude and longitude must not be null");
    }

    School school =
        schoolRepositoryPort
            .findById(command.schoolId())
            .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada"));

    ProximityRange initialRange =
        ProximityRange.fromCoordinates(
            command.latitude(), command.longitude(), school.latitude(), school.longitude());

    PickupQueueItem newItem =
        new PickupQueueItem(
            null, command.schoolId(), command.studentId(), command.parentId(), initialRange);
    newItem.updateLocation(command.latitude(), command.longitude());

    PickupQueueItem saved = queueRepositoryPort.save(newItem);
    notificationPort.notifyStudentArrivalAnnounced(saved);

    return saved;
  }
}
