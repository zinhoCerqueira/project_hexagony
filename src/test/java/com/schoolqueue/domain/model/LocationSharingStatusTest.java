package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocationSharingStatusTest {

  @Test
  @DisplayName("defines the location sharing states in specification order")
  void shouldDefineAllExpectedStates() {
    assertThat(LocationSharingStatus.values())
        .containsExactly(LocationSharingStatus.ACTIVE, LocationSharingStatus.EXPIRED);
  }
}
