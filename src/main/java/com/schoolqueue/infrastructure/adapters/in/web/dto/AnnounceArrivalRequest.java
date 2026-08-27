package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AnnounceArrivalRequest(
    @NotNull UUID schoolId,
    @NotNull UUID studentId,
    @NotNull UUID parentId,
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude) {}
