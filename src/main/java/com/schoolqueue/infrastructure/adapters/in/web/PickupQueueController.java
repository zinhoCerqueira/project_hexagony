package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateQueueStatusCommand;
import com.schoolqueue.infrastructure.adapters.in.web.dto.AnnounceArrivalRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.QueueItemResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateStatusRequest;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.QueueActionMapper;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.QueueDtoMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queue")
public class PickupQueueController {

  private final AnnounceArrivalUseCase announceArrivalUseCase;
  private final UpdateQueueStatusUseCase updateQueueStatusUseCase;
  private final FetchActiveQueueUseCase fetchActiveQueueUseCase;

  public PickupQueueController(
      AnnounceArrivalUseCase announceArrivalUseCase,
      UpdateQueueStatusUseCase updateQueueStatusUseCase,
      FetchActiveQueueUseCase fetchActiveQueueUseCase) {
    this.announceArrivalUseCase = announceArrivalUseCase;
    this.updateQueueStatusUseCase = updateQueueStatusUseCase;
    this.fetchActiveQueueUseCase = fetchActiveQueueUseCase;
  }

  @PostMapping("/announce")
  public ResponseEntity<QueueItemResponse> announce(
      @Valid @RequestBody AnnounceArrivalRequest request) {
    PickupQueueItem item = announceArrivalUseCase.execute(QueueDtoMapper.toCommand(request));
    return ResponseEntity.ok(QueueDtoMapper.toResponse(item));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<QueueItemResponse> updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
    UpdateQueueStatusCommand command =
        new UpdateQueueStatusCommand(id, QueueActionMapper.toAction(request));
    PickupQueueItem item = updateQueueStatusUseCase.execute(command);
    return ResponseEntity.ok(QueueDtoMapper.toResponse(item));
  }

  @GetMapping("/school/{schoolId}/active")
  public ResponseEntity<List<QueueItemResponse>> activeQueue(@PathVariable UUID schoolId) {
    List<PickupQueueItem> items = fetchActiveQueueUseCase.execute(schoolId);
    List<QueueItemResponse> body = items.stream().map(QueueDtoMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }
}
