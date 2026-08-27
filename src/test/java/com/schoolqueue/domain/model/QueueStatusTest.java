package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueueStatusTest {

  @Test
  @DisplayName("defines the queue states in specification order")
  void shouldDefineAllExpectedStates() {
    assertThat(QueueStatus.values())
        .containsExactly(
            QueueStatus.EN_ROUTE,
            QueueStatus.ARRIVED,
            QueueStatus.COMPLETED,
            QueueStatus.CANCELLED);
  }

  @Test
  @DisplayName("activeStatuses returns EN_ROUTE and ARRIVED only")
  void shouldExposeOnlyEnRouteAndArrivedAsActiveStatuses() {
    Set<QueueStatus> active = QueueStatus.activeStatuses();

    assertThat(active).containsExactlyInAnyOrder(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED);
  }
}
