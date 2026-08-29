package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.mapper.SchoolEntityMapper;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataSchoolRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SchoolPersistenceAdapter implements SchoolRepositoryPort {

  private final SpringDataSchoolRepository repository;

  public SchoolPersistenceAdapter(SpringDataSchoolRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<School> findById(UUID id) {
    return repository.findById(id).map(SchoolEntityMapper::toDomain);
  }

  @Override
  public List<School> findAll() {
    return repository.findAll().stream().map(SchoolEntityMapper::toDomain).toList();
  }

  @Override
  public School save(School school) {
    return SchoolEntityMapper.toDomain(repository.save(SchoolEntityMapper.toEntity(school)));
  }
}
