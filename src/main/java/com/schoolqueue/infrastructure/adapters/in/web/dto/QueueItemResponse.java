package com.schoolqueue.infrastructure.adapters.in.web.dto;

import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QueueItemResponse(
    UUID id,
    UUID schoolId,
    UUID studentId,
    UUID parentId,
    QueueStatus journeyStatus,
    boolean called,
    ProximityRange currentRange,
    BigDecimal latitude,
    BigDecimal longitude,
    Instant createdAt,
    Instant updatedAt) {}
