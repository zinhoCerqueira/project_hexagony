package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.SchoolEntity;

public final class SchoolEntityMapper {

  private SchoolEntityMapper() {}

  public static SchoolEntity toEntity(School school) {
    return new SchoolEntity(school.id(), school.name(), school.latitude(), school.longitude());
  }

  public static School toDomain(SchoolEntity entity) {
    return new School(
        entity.getId(), entity.getName(), entity.getLatitude(), entity.getLongitude());
  }
}
