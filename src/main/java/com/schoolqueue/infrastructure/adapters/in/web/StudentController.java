package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.FetchStudentUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsByClassroomUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase;
import com.schoolqueue.domain.ports.in.UpdateStudentUseCase;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterStudentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.StudentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateStudentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.StudentDtoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

  private final RegisterStudentUseCase registerStudentUseCase;
  private final FetchStudentUseCase fetchStudentUseCase;
  private final ListStudentsBySchoolUseCase listStudentsBySchoolUseCase;
  private final ListStudentsByClassroomUseCase listStudentsByClassroomUseCase;
  private final UpdateStudentUseCase updateStudentUseCase;
  private final ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort;

  public StudentController(
      RegisterStudentUseCase registerStudentUseCase,
      FetchStudentUseCase fetchStudentUseCase,
      ListStudentsBySchoolUseCase listStudentsBySchoolUseCase,
      ListStudentsByClassroomUseCase listStudentsByClassroomUseCase,
      UpdateStudentUseCase updateStudentUseCase,
      ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort) {
    this.registerStudentUseCase = registerStudentUseCase;
    this.fetchStudentUseCase = fetchStudentUseCase;
    this.listStudentsBySchoolUseCase = listStudentsBySchoolUseCase;
    this.listStudentsByClassroomUseCase = listStudentsByClassroomUseCase;
    this.updateStudentUseCase = updateStudentUseCase;
    this.parentStudentLinkRepositoryPort = parentStudentLinkRepositoryPort;
  }

  @PostMapping
  public ResponseEntity<StudentResponse> register(@Valid @RequestBody RegisterStudentRequest request) {
    Student student = registerStudentUseCase.execute(StudentDtoMapper.toCommand(request));
    URI location = URI.create("/api/v1/students/" + student.id());
    return ResponseEntity.created(location)
        .body(StudentDtoMapper.toResponse(student, parentStudentLinkRepositoryPort));
  }

  @GetMapping("/{id}")
  public ResponseEntity<StudentResponse> fetch(@PathVariable UUID id) {
    Student student = fetchStudentUseCase.execute(id);
    return ResponseEntity.ok(StudentDtoMapper.toResponse(student, parentStudentLinkRepositoryPort));
  }

  @GetMapping("/school/{schoolId}")
  public ResponseEntity<List<StudentResponse>> listBySchool(@PathVariable UUID schoolId) {
    List<StudentResponse> body =
        listStudentsBySchoolUseCase.execute(schoolId).stream()
            .map(s -> StudentDtoMapper.toResponse(s, parentStudentLinkRepositoryPort))
            .toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/classroom/{classroomId}")
  public ResponseEntity<List<StudentResponse>> listByClassroom(@PathVariable UUID classroomId) {
    List<StudentResponse> body =
        listStudentsByClassroomUseCase.execute(classroomId).stream()
            .map(s -> StudentDtoMapper.toResponse(s, parentStudentLinkRepositoryPort))
            .toList();
    return ResponseEntity.ok(body);
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateStudentRequest request) {
    Student student = updateStudentUseCase.execute(StudentDtoMapper.toCommand(id, request));
    return ResponseEntity.ok(StudentDtoMapper.toResponse(student, parentStudentLinkRepositoryPort));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
  }
}
