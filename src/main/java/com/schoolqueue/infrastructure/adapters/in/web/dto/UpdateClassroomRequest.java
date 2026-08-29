package com.schoolqueue.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateClassroomRequest(@NotNull UUID schoolId, @NotBlank String name) {}
