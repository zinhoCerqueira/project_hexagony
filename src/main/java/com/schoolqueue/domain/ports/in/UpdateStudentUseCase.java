package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Student;
import java.util.List;
import java.util.UUID;

public interface UpdateStudentUseCase {

  Student execute(UpdateStudentCommand command);

  record UpdateStudentCommand(
      UUID id, UUID schoolId, UUID classroomId, String name, List<UUID> parentIds) {}
}
