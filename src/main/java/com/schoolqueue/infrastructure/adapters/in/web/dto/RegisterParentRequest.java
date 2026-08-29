package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterParentRequest(@NotBlank String name, @NotBlank String phone) {}
