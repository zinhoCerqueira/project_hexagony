package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.SchoolEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSchoolRepository extends JpaRepository<SchoolEntity, UUID> {}
