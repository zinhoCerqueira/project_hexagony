package com.schoolqueue.domain.model;

import java.util.Set;

public enum QueueStatus {
  EN_ROUTE,
  ARRIVED,
  COMPLETED,
  CANCELLED;

  public static Set<QueueStatus> activeStatuses() {
    return Set.of(EN_ROUTE, ARRIVED);
  }
}
