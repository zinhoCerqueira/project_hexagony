package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.Student;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepositoryPort {

  Optional<Student> findById(UUID id);

  Student save(Student student);
}
