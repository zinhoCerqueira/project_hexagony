package com.schoolqueue.infrastructure.adapters.out.messaging.dto;

import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import java.time.Instant;
import java.util.UUID;

public record ArrivalAnnouncedEvent(
    UUID queueItemId,
    UUID studentId,
    UUID schoolId,
    QueueStatus status,
    boolean called,
    ProximityRange currentRange,
    Instant occurredAt) {}
