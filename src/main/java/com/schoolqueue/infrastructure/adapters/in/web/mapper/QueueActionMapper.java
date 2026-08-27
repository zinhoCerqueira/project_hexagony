package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.QueueAction;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateStatusRequest;

public final class QueueActionMapper {

  private QueueActionMapper() {}

  public static QueueAction toAction(UpdateStatusRequest request) {
    if (request.action() == null) {
      throw new IllegalStateException("action is required");
    }
    return switch (request.action()) {
      case "UPDATE_RANGE" -> {
        if (request.newRange() == null) {
          throw new IllegalStateException("newRange is required for UPDATE_RANGE");
        }
        yield new com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateRange(
            request.newRange());
      }
      case "MARK_AS_ARRIVED" ->
          new com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsArrived();
      case "MARK_AS_COMPLETED" ->
          new com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsCompleted();
      case "CANCEL" -> new com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.Cancel();
      default -> throw new IllegalStateException("Unknown action: " + request.action());
    };
  }
}
