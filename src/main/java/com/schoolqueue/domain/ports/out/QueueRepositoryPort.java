package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepositoryPort {

  PickupQueueItem save(PickupQueueItem item);

  Optional<PickupQueueItem> findById(UUID id);

  List<PickupQueueItem> findBySchoolIdAndStatusIn(UUID schoolId, List<QueueStatus> statuses);

  Optional<PickupQueueItem> findActiveByStudentId(UUID studentId);
}
