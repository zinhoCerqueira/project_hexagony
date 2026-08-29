package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Classroom;
import java.util.UUID;

public interface FetchClassroomUseCase {

  Classroom execute(UUID id);
}
