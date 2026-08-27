package com.schoolqueue.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SchoolResponse(UUID id, String name, BigDecimal latitude, BigDecimal longitude) {}
