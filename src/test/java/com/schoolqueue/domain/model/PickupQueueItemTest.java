package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
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

  @Test
  @DisplayName("transitions to ARRIVED when EN_ROUTE")
  void shouldTransitionToArrivedWhenEnRoute() {
    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
    Instant before = Instant.now();

    item.markAsArrived();

    assertThat(item.status()).isEqualTo(QueueStatus.ARRIVED);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when not EN_ROUTE")
  void shouldThrowInvalidQueueStateExceptionWhenNotEnRoute() {
    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
    item.markAsArrived();

    assertThatThrownBy(() -> item.markAsArrived())
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Apenas responsáveis a caminho podem ser marcados como 'Chegou'");
  }

  @Test
  @DisplayName("transitions to COMPLETED when ARRIVED")
  void shouldTransitionToCompletedWhenArrived() {
    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
    item.markAsArrived();
    Instant before = Instant.now();

    item.markAsCompleted();

    assertThat(item.status()).isEqualTo(QueueStatus.COMPLETED);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when EN_ROUTE")
  void shouldThrowInvalidQueueStateExceptionWhenEnRoute() {
    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);

    assertThatThrownBy(() -> item.markAsCompleted())
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Aluno não pode ser entregue sem ter chegado");
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when already COMPLETED")
  void shouldThrowInvalidQueueStateExceptionWhenAlreadyCompleted() {
    PickupQueueItem item =
        new PickupQueueItem(null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
    item.markAsArrived();
    item.markAsCompleted();

    assertThatThrownBy(() -> item.markAsCompleted())
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Aluno não pode ser entregue sem ter chegado");
  }
}
