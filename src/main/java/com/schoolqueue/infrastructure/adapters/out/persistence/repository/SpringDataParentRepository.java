package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataParentRepository extends JpaRepository<ParentEntity, UUID> {}
