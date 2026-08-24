package com.schoolqueue.infrastructure.adapters.out.persistence.entity;

import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pickup_queue")
public class PickupQueueEntity {

  @Id private UUID id;

  @Column(name = "school_id")
  private UUID schoolId;

  @Column(name = "student_id")
  private UUID studentId;

  @Column(name = "parent_id")
  private UUID parentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "journey_status", nullable = false)
  private QueueStatus journeyStatus;

  @Column(nullable = false)
  private boolean called;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_range", nullable = false)
  private ProximityRange currentRange;

  @Column(name = "estimated_eta_minutes")
  private Integer estimatedEtaMinutes;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected PickupQueueEntity() {}

  public PickupQueueEntity(
      UUID id,
      UUID schoolId,
      UUID studentId,
      UUID parentId,
      QueueStatus journeyStatus,
      boolean called,
      ProximityRange currentRange,
      Integer estimatedEtaMinutes,
      BigDecimal latitude,
      BigDecimal longitude,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.schoolId = schoolId;
    this.studentId = studentId;
    this.parentId = parentId;
    this.journeyStatus = journeyStatus;
    this.called = called;
    this.currentRange = currentRange;
    this.estimatedEtaMinutes = estimatedEtaMinutes;
    this.latitude = latitude;
    this.longitude = longitude;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSchoolId() {
    return schoolId;
  }

  public void setSchoolId(UUID schoolId) {
    this.schoolId = schoolId;
  }

  public UUID getStudentId() {
    return studentId;
  }

  public void setStudentId(UUID studentId) {
    this.studentId = studentId;
  }

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public QueueStatus getJourneyStatus() {
    return journeyStatus;
  }

  public void setJourneyStatus(QueueStatus journeyStatus) {
    this.journeyStatus = journeyStatus;
  }

  public boolean isCalled() {
    return called;
  }

  public void setCalled(boolean called) {
    this.called = called;
  }

  public ProximityRange getCurrentRange() {
    return currentRange;
  }

  public void setCurrentRange(ProximityRange currentRange) {
    this.currentRange = currentRange;
  }

  public Integer getEstimatedEtaMinutes() {
    return estimatedEtaMinutes;
  }

  public void setEstimatedEtaMinutes(Integer estimatedEtaMinutes) {
    this.estimatedEtaMinutes = estimatedEtaMinutes;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
