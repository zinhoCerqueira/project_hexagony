package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.PickupQueueItem;
import java.math.BigDecimal;
import java.util.UUID;

public interface AnnounceArrivalUseCase {

  PickupQueueItem execute(AnnounceArrivalCommand command);

  record AnnounceArrivalCommand(
      UUID schoolId,
      UUID studentId,
      UUID parentId,
      BigDecimal latitude,
      BigDecimal longitude,
      Integer etaMinutes) {}
}
