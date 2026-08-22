package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.School;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepositoryPort {

  Optional<School> findById(UUID id);

  School save(School school);
}
