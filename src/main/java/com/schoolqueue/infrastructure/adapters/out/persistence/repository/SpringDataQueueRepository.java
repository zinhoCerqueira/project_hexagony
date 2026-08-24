package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.PickupQueueEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataQueueRepository extends JpaRepository<PickupQueueEntity, UUID> {

  List<PickupQueueEntity> findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc(
      UUID schoolId, Collection<QueueStatus> statuses);

  Optional<PickupQueueEntity> findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtAsc(
      UUID studentId, Collection<QueueStatus> statuses);
}
