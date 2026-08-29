package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.mapper.ParentEntityMapper;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataParentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ParentPersistenceAdapter implements ParentRepositoryPort {

  private final SpringDataParentRepository repository;

  public ParentPersistenceAdapter(SpringDataParentRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Parent> findById(UUID id) {
    return repository.findById(id).map(ParentEntityMapper::toDomain);
  }

  @Override
  public List<Parent> findAll() {
    return repository.findAll().stream().map(ParentEntityMapper::toDomain).toList();
  }

  @Override
  public Parent save(Parent parent) {
    return ParentEntityMapper.toDomain(repository.save(ParentEntityMapper.toEntity(parent)));
  }
}
