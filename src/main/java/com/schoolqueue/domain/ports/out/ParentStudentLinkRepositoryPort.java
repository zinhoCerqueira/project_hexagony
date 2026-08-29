package com.schoolqueue.domain.ports.out;

import java.util.List;
import java.util.UUID;

public interface ParentStudentLinkRepositoryPort {

  List<UUID> findParentsOfStudent(UUID studentId);

  void replaceParentsOfStudent(UUID studentId, List<UUID> parentIds);

  List<UUID> findStudentsOfParent(UUID parentId);
}
