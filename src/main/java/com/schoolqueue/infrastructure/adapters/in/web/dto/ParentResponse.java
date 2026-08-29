package com.schoolqueue.infrastructure.adapters.in.web.dto;

import java.util.UUID;

public record ParentResponse(UUID id, String name, String phone) {}
