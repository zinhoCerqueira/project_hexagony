package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentSchoolEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentSchoolEntity.ParentSchoolId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataParentSchoolRepository
    extends JpaRepository<ParentSchoolEntity, ParentSchoolId> {

  @Query("SELECT ps.id.schoolId FROM ParentSchoolEntity ps WHERE ps.id.parentId = :parentId")
  List<UUID> findSchoolIdsByParentId(@Param("parentId") UUID parentId);
}
