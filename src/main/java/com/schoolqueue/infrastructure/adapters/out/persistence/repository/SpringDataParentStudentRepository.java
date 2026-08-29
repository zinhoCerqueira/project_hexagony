package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentStudentEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentStudentEntity.ParentStudentId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataParentStudentRepository
    extends JpaRepository<ParentStudentEntity, ParentStudentId> {

  @Query("SELECT ps.id.parentId FROM ParentStudentEntity ps WHERE ps.id.studentId = :studentId")
  List<UUID> findParentIdsByStudentId(@Param("studentId") UUID studentId);

  @Query("SELECT ps.id.studentId FROM ParentStudentEntity ps WHERE ps.id.parentId = :parentId")
  List<UUID> findStudentIdsByParentId(@Param("parentId") UUID parentId);

  @Modifying
  @Query("DELETE FROM ParentStudentEntity ps WHERE ps.id.studentId = :studentId")
  void deleteByStudentId(@Param("studentId") UUID studentId);
}
