package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PickupQueueItemTest {

  private PickupQueueItem newItem(ProximityRange initialRange) {
    return new PickupQueueItem(
        null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10, initialRange);
  }

  @Test
  @DisplayName("exposes all fields when constructed with an explicit id")
  void shouldExposeAllFieldsWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();

    PickupQueueItem item =
        new PickupQueueItem(id, schoolId, studentId, parentId, 10, ProximityRange.FAR);

    assertThat(item.id()).isEqualTo(id);
    assertThat(item.schoolId()).isEqualTo(schoolId);
    assertThat(item.studentId()).isEqualTo(studentId);
    assertThat(item.parentId()).isEqualTo(parentId);
    assertThat(item.estimatedEtaMinutes()).isEqualTo(10);
    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(item.called()).isFalse();
    assertThat(item.currentRange()).isEqualTo(ProximityRange.FAR);
    assertThat(item.latitude()).isNull();
    assertThat(item.longitude()).isNull();
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    PickupQueueItem item = newItem(ProximityRange.MEDIUM);

    assertThat(item.id()).isNotNull();
    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("initializes createdAt and updatedAt on construction")
  void shouldInitializeCreatedAtAndUpdatedAtOnConstruction() {
    Instant before = Instant.now();

    PickupQueueItem item = newItem(ProximityRange.FAR);

    assertThat(item.createdAt()).isNotNull().isAfterOrEqualTo(before);
    assertThat(item.updatedAt()).isNotNull().isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("starts as called when the initial range is CLOSE")
  void shouldStartCalledWhenInitialRangeIsClose() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);

    assertThat(item.called()).isTrue();
    assertThat(item.currentRange()).isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("updates the current range and bumps updatedAt")
  void shouldUpdateRangeAndBumpUpdatedAt() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    Instant before = Instant.now();

    item.updateRange(ProximityRange.MEDIUM);

    assertThat(item.currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("auto-calls the student when entering the CLOSE range")
  void shouldAutoCallStudentWhenEnteringCloseRange() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    Instant before = Instant.now();

    item.updateRange(ProximityRange.CLOSE);

    assertThat(item.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(item.called()).isTrue();
    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("keeps the called flag when leaving and re-entering ranges")
  void shouldKeepCalledFlagWhenChangingRangesAfterCall() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);

    item.updateRange(ProximityRange.MEDIUM);
    item.updateRange(ProximityRange.FAR);

    assertThat(item.currentRange()).isEqualTo(ProximityRange.FAR);
    assertThat(item.called()).isTrue();
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when updating range after completion")
  void shouldThrowInvalidQueueStateExceptionWhenUpdatingRangeAfterCompleted() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);
    item.markAsCompleted();

    assertThatThrownBy(() -> item.updateRange(ProximityRange.FAR))
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Fila já finalizada ou cancelada");
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when updating range after cancellation")
  void shouldThrowInvalidQueueStateExceptionWhenUpdatingRangeAfterCancelled() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    item.cancel();

    assertThatThrownBy(() -> item.updateRange(ProximityRange.CLOSE))
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Fila já finalizada ou cancelada");
  }

  @Test
  @DisplayName("transitions to ARRIVED when EN_ROUTE")
  void shouldTransitionToArrivedWhenEnRoute() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    Instant before = Instant.now();

    item.markAsArrived();

    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.ARRIVED);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when marking as arrived when not EN_ROUTE")
  void shouldThrowInvalidQueueStateExceptionWhenMarkingArrivedWhenNotEnRoute() {
    PickupQueueItem arrived = newItem(ProximityRange.FAR);
    arrived.markAsArrived();

    assertThatThrownBy(arrived::markAsArrived)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Apenas responsáveis a caminho podem ser marcados como 'Chegou'");

    PickupQueueItem completed = newItem(ProximityRange.CLOSE);
    completed.markAsCompleted();

    assertThatThrownBy(completed::markAsArrived)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Apenas responsáveis a caminho podem ser marcados como 'Chegou'");

    PickupQueueItem cancelled = newItem(ProximityRange.FAR);
    cancelled.cancel();

    assertThatThrownBy(cancelled::markAsArrived)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Apenas responsáveis a caminho podem ser marcados como 'Chegou'");
  }

  @Test
  @DisplayName("completes while EN_ROUTE when already called by GPS")
  void shouldCompleteWhileEnRouteWhenAlreadyCalledByGps() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);
    Instant before = Instant.now();

    item.markAsCompleted();

    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.COMPLETED);
    assertThat(item.journeyStatus()).isNotEqualTo(QueueStatus.ARRIVED);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("completes when ARRIVED and called")
  void shouldCompleteWhenArrivedAndCalled() {
    PickupQueueItem item = newItem(ProximityRange.FAR);
    item.markAsArrived();
    item.updateRange(ProximityRange.CLOSE);
    Instant before = Instant.now();

    item.markAsCompleted();

    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.COMPLETED);
    assertThat(item.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when completing without being called")
  void shouldThrowInvalidQueueStateExceptionWhenCompletingWithoutBeingCalled() {
    PickupQueueItem enRoute = newItem(ProximityRange.FAR);

    assertThatThrownBy(enRoute::markAsCompleted)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Aluno não pode ser entregue sem ter sido chamado");

    PickupQueueItem arrived = newItem(ProximityRange.MEDIUM);
    arrived.markAsArrived();

    assertThatThrownBy(arrived::markAsCompleted)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Aluno não pode ser entregue sem ter sido chamado");
  }

  @Test
  @DisplayName("cancels from EN_ROUTE and ARRIVED")
  void shouldCancelFromEnRouteAndArrived() {
    PickupQueueItem enRoute = newItem(ProximityRange.FAR);
    Instant before = Instant.now();

    enRoute.cancel();

    assertThat(enRoute.journeyStatus()).isEqualTo(QueueStatus.CANCELLED);
    assertThat(enRoute.updatedAt()).isAfterOrEqualTo(before);

    PickupQueueItem arrived = newItem(ProximityRange.MEDIUM);
    arrived.markAsArrived();
    arrived.cancel();

    assertThat(arrived.journeyStatus()).isEqualTo(QueueStatus.CANCELLED);
  }

  @Test
  @DisplayName("throws InvalidQueueStateException when cancelling a completed item")
  void shouldThrowInvalidQueueStateExceptionWhenCancellingCompleted() {
    PickupQueueItem item = newItem(ProximityRange.CLOSE);
    item.markAsCompleted();

    assertThatThrownBy(item::cancel)
        .isInstanceOf(InvalidQueueStateException.class)
        .hasMessage("Entrega concluída não pode ser cancelada");
  }
}
