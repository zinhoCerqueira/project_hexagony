package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.mapper.QueueEntityMapper;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataQueueRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QueuePersistenceAdapter implements QueueRepositoryPort {

  private final SpringDataQueueRepository repository;

  public QueuePersistenceAdapter(SpringDataQueueRepository repository) {
    this.repository = repository;
  }

  @Override
  public PickupQueueItem save(PickupQueueItem item) {
    PickupQueueItem toPersist = item.id() == null ? item.withId(java.util.UUID.randomUUID()) : item;
    return QueueEntityMapper.toDomain(repository.save(QueueEntityMapper.toEntity(toPersist)));
  }

  @Override
  public Optional<PickupQueueItem> findById(UUID id) {
    return repository.findById(id).map(QueueEntityMapper::toDomain);
  }

  @Override
  public List<PickupQueueItem> findBySchoolIdAndStatusIn(
      UUID schoolId, List<QueueStatus> statuses) {
    return repository
        .findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc(schoolId, statuses)
        .stream()
        .map(QueueEntityMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<PickupQueueItem> findActiveByStudentId(UUID studentId) {
    return repository
        .findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc(
            studentId, QueueStatus.activeStatuses())
        .map(QueueEntityMapper::toDomain);
  }
}
