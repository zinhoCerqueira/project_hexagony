package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Classroom;
import java.util.List;
import java.util.UUID;

public interface ListClassroomsBySchoolUseCase {

  List<Classroom> execute(UUID schoolId);
}
