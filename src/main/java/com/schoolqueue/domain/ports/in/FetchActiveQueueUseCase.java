package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.PickupQueueItem;
import java.util.List;
import java.util.UUID;

public interface FetchActiveQueueUseCase {

  List<PickupQueueItem> execute(UUID schoolId);
}
