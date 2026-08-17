package com.schoolqueue.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PickupQueueItem {
  private final UUID id;
  private final UUID schoolId;
  private final UUID studentId;
  private final UUID parentId;
  private final Instant createdAt;
  private QueueStatus status;
  private Integer estimatedEtaMinutes;
  private Instant updatedAt;

  public PickupQueueItem(
      UUID id, UUID schoolId, UUID studentId, UUID parentId, Integer estimatedEtaMinutes) {
    this.id = id != null ? id : UUID.randomUUID();
    this.schoolId = schoolId;
    this.studentId = studentId;
    this.parentId = parentId;
    this.status = QueueStatus.EN_ROUTE;
    this.estimatedEtaMinutes = estimatedEtaMinutes;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public UUID id() {
    return id;
  }

  public UUID schoolId() {
    return schoolId;
  }

  public UUID studentId() {
    return studentId;
  }

  public UUID parentId() {
    return parentId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public QueueStatus status() {
    return status;
  }

  public Integer estimatedEtaMinutes() {
    return estimatedEtaMinutes;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
