package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.mapper.ClassroomEntityMapper;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataClassroomRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClassroomPersistenceAdapter implements ClassroomRepositoryPort {

  private final SpringDataClassroomRepository repository;

  public ClassroomPersistenceAdapter(SpringDataClassroomRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Classroom> findById(UUID id) {
    return repository.findById(id).map(ClassroomEntityMapper::toDomain);
  }

  @Override
  public List<Classroom> findBySchoolId(UUID schoolId) {
    return repository.findBySchoolId(schoolId).stream()
        .map(ClassroomEntityMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  @Override
  public Classroom save(Classroom classroom) {
    return ClassroomEntityMapper.toDomain(repository.save(ClassroomEntityMapper.toEntity(classroom)));
  }
}
