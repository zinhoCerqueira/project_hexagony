package com.schoolqueue.infrastructure.adapters.out.persistence;

import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentStudentEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.repository.SpringDataParentStudentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ParentStudentLinkPersistenceAdapter implements ParentStudentLinkRepositoryPort {

  private final SpringDataParentStudentRepository repository;

  public ParentStudentLinkPersistenceAdapter(SpringDataParentStudentRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<UUID> findParentsOfStudent(UUID studentId) {
    return repository.findParentIdsByStudentId(studentId);
  }

  @Override
  public List<UUID> findStudentsOfParent(UUID parentId) {
    return repository.findStudentIdsByParentId(parentId);
  }

  @Override
  @Transactional
  public void replaceParentsOfStudent(UUID studentId, List<UUID> parentIds) {
    repository.deleteByStudentId(studentId);
    repository.flush();
    for (UUID parentId : parentIds) {
      repository.save(new ParentStudentEntity(parentId, studentId));
    }
  }
}
