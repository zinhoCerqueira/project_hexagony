package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.PickupQueueEntity;

public final class QueueEntityMapper {

  private QueueEntityMapper() {}

  public static PickupQueueEntity toEntity(PickupQueueItem item) {
    return new PickupQueueEntity(
        item.id(),
        item.schoolId(),
        item.studentId(),
        item.parentId(),
        item.journeyStatus(),
        item.called(),
        item.currentRange(),
        item.latitude(),
        item.longitude(),
        item.createdAt(),
        item.updatedAt());
  }

  public static PickupQueueItem toDomain(PickupQueueEntity entity) {
    return PickupQueueItem.reconstitute(
        entity.getId(),
        entity.getSchoolId(),
        entity.getStudentId(),
        entity.getParentId(),
        entity.getJourneyStatus(),
        entity.isCalled(),
        entity.getCurrentRange(),
        entity.getLatitude(),
        entity.getLongitude(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
