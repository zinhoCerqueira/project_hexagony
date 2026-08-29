package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Student;
import java.util.List;
import java.util.UUID;

public interface FetchStudentUseCase {

  Student execute(UUID id);
}
