package com.schoolqueue.infrastructure.adapters.in.web.dto;

import java.util.UUID;

public record ClassroomResponse(UUID id, UUID schoolId, String name) {}
