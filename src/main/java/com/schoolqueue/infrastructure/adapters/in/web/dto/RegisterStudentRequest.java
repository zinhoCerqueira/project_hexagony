package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RegisterStudentRequest(
    @NotNull UUID schoolId,
    @NotNull UUID classroomId,
    @NotBlank String name,
    @NotEmpty List<UUID> parentIds) {}
