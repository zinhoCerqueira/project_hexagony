package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.StudentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStudentRepository extends JpaRepository<StudentEntity, UUID> {

  List<StudentEntity> findBySchoolId(UUID schoolId);

  List<StudentEntity> findByClassroomId(UUID classroomId);
}
