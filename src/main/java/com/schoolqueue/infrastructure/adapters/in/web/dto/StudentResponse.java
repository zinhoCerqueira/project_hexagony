package com.schoolqueue.infrastructure.adapters.in.web.dto;

import java.util.List;
import java.util.UUID;

public record StudentResponse(
    UUID id, UUID schoolId, UUID classroomId, String name, List<UUID> parentIds) {}
