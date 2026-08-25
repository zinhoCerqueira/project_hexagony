package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.PickupQueueEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueueEntityMapperTest {

  private PickupQueueItem newDomainItem() {
    return PickupQueueItem.reconstitute(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        QueueStatus.COMPLETED,
        true,
        ProximityRange.CLOSE,
        new BigDecimal("-23.5505"),
        new BigDecimal("-46.6333"),
        10,
        Instant.parse("2026-08-24T12:00:00Z"),
        Instant.parse("2026-08-24T12:05:00Z"));
  }

  private PickupQueueEntity newEntity() {
    return new PickupQueueEntity(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        QueueStatus.ARRIVED,
        false,
        ProximityRange.MEDIUM,
        null,
        null,
        null,
        Instant.parse("2026-08-24T13:00:00Z"),
        Instant.parse("2026-08-24T13:10:00Z"));
  }

  @Test
  @DisplayName("converts domain to entity preserving every field")
  void shouldConvertDomainToEntityPreservingEveryField() {
    PickupQueueItem item = newDomainItem();

    PickupQueueEntity entity = QueueEntityMapper.toEntity(item);

    assertThat(entity.getId()).isEqualTo(item.id());
    assertThat(entity.getSchoolId()).isEqualTo(item.schoolId());
    assertThat(entity.getStudentId()).isEqualTo(item.studentId());
    assertThat(entity.getParentId()).isEqualTo(item.parentId());
    assertThat(entity.getJourneyStatus()).isEqualTo(item.journeyStatus());
    assertThat(entity.isCalled()).isEqualTo(item.called());
    assertThat(entity.getCurrentRange()).isEqualTo(item.currentRange());
    assertThat(entity.getLatitude()).isEqualByComparingTo(item.latitude());
    assertThat(entity.getLongitude()).isEqualByComparingTo(item.longitude());
    assertThat(entity.getEstimatedEtaMinutes()).isEqualTo(item.estimatedEtaMinutes());
    assertThat(entity.getCreatedAt()).isEqualTo(item.createdAt());
    assertThat(entity.getUpdatedAt()).isEqualTo(item.updatedAt());
  }

  @Test
  @DisplayName("converts entity to domain preserving the full persisted state")
  void shouldConvertEntityToDomainPreservingFullPersistedState() {
    PickupQueueEntity entity = newEntity();

    PickupQueueItem item = QueueEntityMapper.toDomain(entity);

    assertThat(item.id()).isEqualTo(entity.getId());
    assertThat(item.schoolId()).isEqualTo(entity.getSchoolId());
    assertThat(item.studentId()).isEqualTo(entity.getStudentId());
    assertThat(item.parentId()).isEqualTo(entity.getParentId());
    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.ARRIVED);
    assertThat(item.called()).isFalse();
    assertThat(item.currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(item.latitude()).isNull();
    assertThat(item.longitude()).isNull();
    assertThat(item.estimatedEtaMinutes()).isNull();
    assertThat(item.createdAt()).isEqualTo(entity.getCreatedAt());
    assertThat(item.updatedAt()).isEqualTo(entity.getUpdatedAt());
  }

  @Test
  @DisplayName("round-trips domain to entity and back without loss")
  void shouldRoundTripDomainToEntityAndBackWithoutLoss() {
    PickupQueueItem item = newDomainItem();

    PickupQueueItem result = QueueEntityMapper.toDomain(QueueEntityMapper.toEntity(item));

    assertThat(result.id()).isEqualTo(item.id());
    assertThat(result.schoolId()).isEqualTo(item.schoolId());
    assertThat(result.studentId()).isEqualTo(item.studentId());
    assertThat(result.parentId()).isEqualTo(item.parentId());
    assertThat(result.journeyStatus()).isEqualTo(item.journeyStatus());
    assertThat(result.called()).isEqualTo(item.called());
    assertThat(result.currentRange()).isEqualTo(item.currentRange());
    assertThat(result.latitude()).isEqualByComparingTo(item.latitude());
    assertThat(result.longitude()).isEqualByComparingTo(item.longitude());
    assertThat(result.estimatedEtaMinutes()).isEqualTo(item.estimatedEtaMinutes());
    assertThat(result.createdAt()).isEqualTo(item.createdAt());
    assertThat(result.updatedAt()).isEqualTo(item.updatedAt());
  }

  @Test
  @DisplayName("keeps optional fields as null in both directions")
  void shouldKeepOptionalFieldsAsNullInBothDirections() {
    PickupQueueItem item = newItemWithoutOptionals();

    PickupQueueEntity entity = QueueEntityMapper.toEntity(item);
    PickupQueueItem result = QueueEntityMapper.toDomain(entity);

    assertThat(entity.getLatitude()).isNull();
    assertThat(entity.getLongitude()).isNull();
    assertThat(entity.getEstimatedEtaMinutes()).isNull();
    assertThat(result.latitude()).isNull();
    assertThat(result.longitude()).isNull();
    assertThat(result.estimatedEtaMinutes()).isNull();
  }

  private PickupQueueItem newItemWithoutOptionals() {
    return PickupQueueItem.reconstitute(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        QueueStatus.EN_ROUTE,
        false,
        ProximityRange.FAR,
        null,
        null,
        null,
        Instant.parse("2026-08-24T14:00:00Z"),
        Instant.parse("2026-08-24T14:01:00Z"));
  }
}
