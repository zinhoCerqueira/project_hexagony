package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchActiveQueueServiceTest {

  @Mock QueueRepositoryPort queueRepositoryPort;

  private PickupQueueItem newItem() {
    return new PickupQueueItem(
        null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10, ProximityRange.FAR);
  }

  private List<PickupQueueItem> newItemsInOrder(int count) throws InterruptedException {
    List<PickupQueueItem> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      items.add(newItem());
      if (i < count - 1) {
        Thread.sleep(5);
      }
    }
    return items;
  }

  @Test
  @DisplayName("returns an empty list when the school has no active items")
  void shouldReturnEmptyListWhenSchoolHasNoActiveItems() {
    UUID schoolId = UUID.randomUUID();
    when(queueRepositoryPort.findBySchoolIdAndStatusIn(
            schoolId, List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED)))
        .thenReturn(List.of());
    FetchActiveQueueService service = new FetchActiveQueueService(queueRepositoryPort);

    assertThat(service.execute(schoolId)).isEmpty();
  }

  @Test
  @DisplayName("queries the repository with the non-terminal statuses")
  void shouldQueryRepositoryWithNonTerminalStatuses() {
    UUID schoolId = UUID.randomUUID();
    when(queueRepositoryPort.findBySchoolIdAndStatusIn(
            schoolId, List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED)))
        .thenReturn(List.of());
    FetchActiveQueueService service = new FetchActiveQueueService(queueRepositoryPort);

    service.execute(schoolId);

    verify(queueRepositoryPort)
        .findBySchoolIdAndStatusIn(schoolId, List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED));
  }

  @Test
  @DisplayName("sorts the active queue by createdAt ascending")
  void shouldSortActiveQueueByCreatedAtAscending() throws InterruptedException {
    UUID schoolId = UUID.randomUUID();
    List<PickupQueueItem> created = newItemsInOrder(3);
    PickupQueueItem first = created.get(0);
    PickupQueueItem second = created.get(1);
    PickupQueueItem third = created.get(2);
    when(queueRepositoryPort.findBySchoolIdAndStatusIn(
            schoolId, List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED)))
        .thenReturn(List.of(third, first, second));
    FetchActiveQueueService service = new FetchActiveQueueService(queueRepositoryPort);

    List<PickupQueueItem> result = service.execute(schoolId);

    assertThat(result).containsExactly(first, second, third);
    assertThat(result.get(0).createdAt()).isBefore(result.get(1).createdAt());
    assertThat(result.get(1).createdAt()).isBefore(result.get(2).createdAt());
  }

  @Test
  @DisplayName("returns every item provided by the repository")
  void shouldReturnEveryItemProvidedByTheRepository() throws InterruptedException {
    UUID schoolId = UUID.randomUUID();
    List<PickupQueueItem> items = newItemsInOrder(4);
    when(queueRepositoryPort.findBySchoolIdAndStatusIn(
            schoolId, List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED)))
        .thenReturn(items);
    FetchActiveQueueService service = new FetchActiveQueueService(queueRepositoryPort);

    List<PickupQueueItem> result = service.execute(schoolId);

    assertThat(result).hasSize(4);
    assertThat(result).containsExactlyInAnyOrderElementsOf(items);
  }
}
