package com.schoolqueue.domain.model;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PickupQueueItem {
  private final UUID id;
  private final UUID schoolId;
  private final UUID studentId;
  private final UUID parentId;
  private QueueStatus journeyStatus;
  private boolean called;
  private ProximityRange currentRange;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private Integer estimatedEtaMinutes;
  private final Instant createdAt;
  private Instant updatedAt;

  public PickupQueueItem(
      UUID id,
      UUID schoolId,
      UUID studentId,
      UUID parentId,
      Integer estimatedEtaMinutes,
      ProximityRange initialRange) {
    this.id = id != null ? id : UUID.randomUUID();
    this.schoolId = schoolId;
    this.studentId = studentId;
    this.parentId = parentId;
    this.journeyStatus = QueueStatus.EN_ROUTE;
    this.called = initialRange == ProximityRange.CLOSE;
    this.currentRange = initialRange;
    this.estimatedEtaMinutes = estimatedEtaMinutes;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  private PickupQueueItem(
      UUID id,
      UUID schoolId,
      UUID studentId,
      UUID parentId,
      QueueStatus journeyStatus,
      boolean called,
      ProximityRange currentRange,
      BigDecimal latitude,
      BigDecimal longitude,
      Integer estimatedEtaMinutes,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.schoolId = schoolId;
    this.studentId = studentId;
    this.parentId = parentId;
    this.journeyStatus = journeyStatus;
    this.called = called;
    this.currentRange = currentRange;
    this.latitude = latitude;
    this.longitude = longitude;
    this.estimatedEtaMinutes = estimatedEtaMinutes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static PickupQueueItem reconstitute(
      UUID id,
      UUID schoolId,
      UUID studentId,
      UUID parentId,
      QueueStatus journeyStatus,
      boolean called,
      ProximityRange currentRange,
      BigDecimal latitude,
      BigDecimal longitude,
      Integer estimatedEtaMinutes,
      Instant createdAt,
      Instant updatedAt) {
    return new PickupQueueItem(
        id,
        schoolId,
        studentId,
        parentId,
        journeyStatus,
        called,
        currentRange,
        latitude,
        longitude,
        estimatedEtaMinutes,
        createdAt,
        updatedAt);
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

  public QueueStatus journeyStatus() {
    return journeyStatus;
  }

  public boolean called() {
    return called;
  }

  public ProximityRange currentRange() {
    return currentRange;
  }

  public BigDecimal latitude() {
    return latitude;
  }

  public BigDecimal longitude() {
    return longitude;
  }

  public Integer estimatedEtaMinutes() {
    return estimatedEtaMinutes;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public void updateRange(ProximityRange newRange) {
    if (this.journeyStatus == QueueStatus.COMPLETED
        || this.journeyStatus == QueueStatus.CANCELLED) {
      throw new InvalidQueueStateException("Fila já finalizada ou cancelada");
    }
    this.currentRange = newRange;
    if (newRange == ProximityRange.CLOSE && !this.called) {
      this.called = true;
    }
    this.updatedAt = Instant.now();
  }

  public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null || longitude == null) {
      throw new IllegalArgumentException("Latitude and longitude must not be null");
    }
    if (this.journeyStatus == QueueStatus.COMPLETED
        || this.journeyStatus == QueueStatus.CANCELLED) {
      throw new InvalidQueueStateException("Fila já finalizada ou cancelada");
    }
    this.latitude = latitude;
    this.longitude = longitude;
    this.updatedAt = Instant.now();
  }

  public void markAsArrived() {
    if (this.journeyStatus != QueueStatus.EN_ROUTE) {
      throw new InvalidQueueStateException(
          "Apenas responsáveis a caminho podem ser marcados como 'Chegou'");
    }
    this.journeyStatus = QueueStatus.ARRIVED;
    this.updatedAt = Instant.now();
  }

  public void markAsCompleted() {
    if (!this.called) {
      throw new InvalidQueueStateException("Aluno não pode ser entregue sem ter sido chamado");
    }
    this.journeyStatus = QueueStatus.COMPLETED;
    this.updatedAt = Instant.now();
  }

  public void cancel() {
    if (this.journeyStatus == QueueStatus.COMPLETED) {
      throw new InvalidQueueStateException("Entrega concluída não pode ser cancelada");
    }
    this.journeyStatus = QueueStatus.CANCELLED;
    this.updatedAt = Instant.now();
  }
}
