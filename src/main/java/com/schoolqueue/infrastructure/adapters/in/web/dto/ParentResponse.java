package com.schoolqueue.infrastructure.adapters.in.web.dto;

import java.util.List;
import java.util.UUID;

public record ParentResponse(
    UUID id, String name, String phone, String email, List<UUID> schoolIds) {}
