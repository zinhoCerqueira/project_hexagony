package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase.AnnounceArrivalCommand;
import com.schoolqueue.infrastructure.adapters.in.web.dto.AnnounceArrivalRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.QueueItemResponse;

public final class QueueDtoMapper {

  private QueueDtoMapper() {}

  public static AnnounceArrivalCommand toCommand(AnnounceArrivalRequest request) {
    return new AnnounceArrivalCommand(
        request.schoolId(),
        request.studentId(),
        request.parentId(),
        request.latitude(),
        request.longitude());
  }

  public static QueueItemResponse toResponse(PickupQueueItem item) {
    return new QueueItemResponse(
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
}
