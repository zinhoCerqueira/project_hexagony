package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.Classroom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepositoryPort {

  Optional<Classroom> findById(UUID id);

  List<Classroom> findBySchoolId(UUID schoolId);

  boolean existsById(UUID id);

  Classroom save(Classroom classroom);
}
