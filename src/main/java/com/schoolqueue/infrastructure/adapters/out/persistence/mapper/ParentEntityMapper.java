package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentEntity;

public final class ParentEntityMapper {

  private ParentEntityMapper() {}

  public static ParentEntity toEntity(Parent parent) {
    return new ParentEntity(parent.id(), parent.name(), parent.phone(), parent.email());
  }

  public static Parent toDomain(ParentEntity entity) {
    return new Parent(entity.getId(), entity.getName(), entity.getPhone(), entity.getEmail());
  }
}
