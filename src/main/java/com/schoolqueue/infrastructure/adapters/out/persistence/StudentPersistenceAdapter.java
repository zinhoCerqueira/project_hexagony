package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.mapper.StudentEntityMapper;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataStudentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StudentPersistenceAdapter implements StudentRepositoryPort {

  private final SpringDataStudentRepository repository;

  public StudentPersistenceAdapter(SpringDataStudentRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Student> findById(UUID id) {
    return repository.findById(id).map(StudentEntityMapper::toDomain);
  }

  @Override
  public List<Student> findBySchoolId(UUID schoolId) {
    return repository.findBySchoolId(schoolId).stream().map(StudentEntityMapper::toDomain).toList();
  }

  @Override
  public List<Student> findByClassroomId(UUID classroomId) {
    return repository.findByClassroomId(classroomId).stream()
        .map(StudentEntityMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  @Override
  public Student save(Student student) {
    return StudentEntityMapper.toDomain(repository.save(StudentEntityMapper.toEntity(student)));
  }
}
