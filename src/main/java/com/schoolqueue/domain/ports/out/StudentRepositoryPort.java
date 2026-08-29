package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepositoryPort {

  Optional<Student> findById(UUID id);

  List<Student> findBySchoolId(UUID schoolId);

  List<Student> findByClassroomId(UUID classroomId);

  boolean existsById(UUID id);

  Student save(Student student);
}
