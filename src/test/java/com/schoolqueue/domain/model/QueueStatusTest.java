package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

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
            QueueStatus.CALLED,
            QueueStatus.COMPLETED,
            QueueStatus.CANCELLED);
  }
}
