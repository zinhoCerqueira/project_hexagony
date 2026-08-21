package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import java.util.UUID;

public interface UpdateQueueStatusUseCase {

  PickupQueueItem execute(UpdateQueueStatusCommand command);

  record UpdateQueueStatusCommand(UUID queueItemId, QueueAction action) {}

  sealed interface QueueAction permits UpdateRange, MarkAsArrived, MarkAsCompleted, Cancel {}

  record UpdateRange(ProximityRange newRange) implements QueueAction {}

  record MarkAsArrived() implements QueueAction {}

  record MarkAsCompleted() implements QueueAction {}

  record Cancel() implements QueueAction {}
}
