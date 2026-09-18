package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateParentRequest(
    @NotBlank String name, @NotBlank String phone, @NotBlank @Email String email) {}
