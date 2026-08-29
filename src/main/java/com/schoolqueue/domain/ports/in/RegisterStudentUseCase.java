package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Student;
import java.util.List;
import java.util.UUID;

public interface RegisterStudentUseCase {

  Student execute(RegisterStudentCommand command);

  record RegisterStudentCommand(UUID schoolId, UUID classroomId, String name, List<UUID> parentIds) {}
}
