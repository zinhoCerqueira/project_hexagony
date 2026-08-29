package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Classroom;
import java.util.UUID;

public interface RegisterClassroomUseCase {

  Classroom execute(RegisterClassroomCommand command);

  record RegisterClassroomCommand(UUID schoolId, String name) {}
}
