package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateSchoolRequest(
    UUID id,
    @NotBlank String name,
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude) {}
