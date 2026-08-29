package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.ListClassroomsBySchoolUseCase;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import java.util.List;
import java.util.UUID;

public class ListClassroomsBySchoolService implements ListClassroomsBySchoolUseCase {

  private final ClassroomRepositoryPort classroomRepositoryPort;

  public ListClassroomsBySchoolService(ClassroomRepositoryPort classroomRepositoryPort) {
    this.classroomRepositoryPort = classroomRepositoryPort;
  }

  @Override
  public List<Classroom> execute(UUID schoolId) {
    return classroomRepositoryPort.findBySchoolId(schoolId);
  }
}
