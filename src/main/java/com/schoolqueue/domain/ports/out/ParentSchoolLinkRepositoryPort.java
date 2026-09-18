package com.schoolqueue.domain.ports.out;

import java.util.List;
import java.util.UUID;

public interface ParentSchoolLinkRepositoryPort {

  void linkSchool(UUID parentId, UUID schoolId);

  List<UUID> findSchoolsOfParent(UUID parentId);
}
