package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FetchActiveQueueService implements FetchActiveQueueUseCase {

  private static final List<QueueStatus> ACTIVE_STATUSES =
      List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED);

  private final QueueRepositoryPort queueRepositoryPort;

  public FetchActiveQueueService(QueueRepositoryPort queueRepositoryPort) {
    this.queueRepositoryPort = queueRepositoryPort;
  }

  @Override
  public List<PickupQueueItem> execute(UUID schoolId) {
    return queueRepositoryPort.findBySchoolIdAndStatusIn(schoolId, ACTIVE_STATUSES).stream()
        .sorted(Comparator.comparing(PickupQueueItem::createdAt))
        .toList();
  }
}
