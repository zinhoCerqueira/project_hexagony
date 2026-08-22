package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;

public interface QueueNotificationPort {

  void notifyStudentArrivalAnnounced(PickupQueueItem item);

  void notifyStatusChanged(PickupQueueItem item, QueueStatus previousStatus);
}
