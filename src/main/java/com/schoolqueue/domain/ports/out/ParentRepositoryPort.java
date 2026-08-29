package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.Parent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentRepositoryPort {

  Optional<Parent> findById(UUID id);

  List<Parent> findAll();

  Parent save(Parent parent);
}
