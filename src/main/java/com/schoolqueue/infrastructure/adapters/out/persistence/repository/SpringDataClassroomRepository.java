package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ClassroomEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataClassroomRepository extends JpaRepository<ClassroomEntity, UUID> {

  List<ClassroomEntity> findBySchoolId(UUID schoolId);
}
