package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PickupQueueItemTest {

  @Test
  @DisplayName("exposes all fields when constructed with an explicit id")
  void shouldExposeAllFieldsWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();

    PickupQueueItem item = new PickupQueueItem(id, schoolId, studentId, parentId, 10);

    assertThat(item.id()).isEqualTo(id);
    assertThat(item.schoolId()).isEqualTo(schoolId);
    assertThat(item.studentId()).isEqualTo(studentId);
    assertThat(item.parentId()).isEqualTo(parentId);
    assertThat(item.estimatedEtaMinutes()).isEqualTo(10);
    assertThat(item.status()).isEqualTo(QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    UUID schoolId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();

    PickupQueueItem item = new PickupQueueItem(null, schoolId, studentId, parentId, 10);

    assertThat(item.id()).isNotNull();
    assertThat(item.schoolId()).isEqualTo(schoolId);
    assertThat(item.studentId()).isEqualTo(studentId);
    assertThat(item.parentId()).isEqualTo(parentId);
    assertThat(item.status()).isEqualTo(QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("initializes createdAt and updatedAt on construction")
  void shouldInitializeCreatedAtAndUpdatedAtOnConstruction() {
    Instant before = Instant.now();

    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);

    assertThat(item.createdAt()).isNotNull().isAfterOrEqualTo(before);
    assertThat(item.updatedAt()).isNotNull().isAfterOrEqualTo(before);
  }
}
