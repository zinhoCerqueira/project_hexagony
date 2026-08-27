package com.schoolqueue.infrastructure.adapters.in.web.dto;

import com.schoolqueue.domain.model.ProximityRange;
import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(@NotBlank String action, ProximityRange newRange) {}
