package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.ports.out.ParentSchoolLinkRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentSchoolEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataParentSchoolRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ParentSchoolLinkPersistenceAdapter implements ParentSchoolLinkRepositoryPort {

  private final SpringDataParentSchoolRepository repository;

  public ParentSchoolLinkPersistenceAdapter(SpringDataParentSchoolRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void linkSchool(UUID parentId, UUID schoolId) {
    repository.save(new ParentSchoolEntity(parentId, schoolId));
  }

  @Override
  public List<UUID> findSchoolsOfParent(UUID parentId) {
    return repository.findSchoolIdsByParentId(parentId);
  }
}
