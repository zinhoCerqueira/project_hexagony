package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterParentRequest(
    @NotBlank String name,
    @NotBlank String phone,
    @NotBlank @Email String email,
    @NotNull UUID schoolId) {}
