package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.FetchClassroomUseCase;
import com.schoolqueue.domain.ports.in.ListClassroomsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ClassroomResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterClassroomRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateClassroomRequest;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.ClassroomDtoMapper;
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
@RequestMapping("/api/v1/classrooms")
public class ClassroomController {

  private final RegisterClassroomUseCase registerClassroomUseCase;
  private final FetchClassroomUseCase fetchClassroomUseCase;
  private final ListClassroomsBySchoolUseCase listClassroomsBySchoolUseCase;
  private final UpdateClassroomUseCase updateClassroomUseCase;

  public ClassroomController(
      RegisterClassroomUseCase registerClassroomUseCase,
      FetchClassroomUseCase fetchClassroomUseCase,
      ListClassroomsBySchoolUseCase listClassroomsBySchoolUseCase,
      UpdateClassroomUseCase updateClassroomUseCase) {
    this.registerClassroomUseCase = registerClassroomUseCase;
    this.fetchClassroomUseCase = fetchClassroomUseCase;
    this.listClassroomsBySchoolUseCase = listClassroomsBySchoolUseCase;
    this.updateClassroomUseCase = updateClassroomUseCase;
  }

  @PostMapping
  public ResponseEntity<ClassroomResponse> register(
      @Valid @RequestBody RegisterClassroomRequest request) {
    Classroom classroom = registerClassroomUseCase.execute(ClassroomDtoMapper.toCommand(request));
    URI location = URI.create("/api/v1/classrooms/" + classroom.id());
    return ResponseEntity.created(location).body(ClassroomDtoMapper.toResponse(classroom));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClassroomResponse> fetch(@PathVariable UUID id) {
    Classroom classroom = fetchClassroomUseCase.execute(id);
    return ResponseEntity.ok(ClassroomDtoMapper.toResponse(classroom));
  }

  @GetMapping("/school/{schoolId}")
  public ResponseEntity<List<ClassroomResponse>> listBySchool(@PathVariable UUID schoolId) {
    List<ClassroomResponse> body =
        listClassroomsBySchoolUseCase.execute(schoolId).stream()
            .map(ClassroomDtoMapper::toResponse)
            .toList();
    return ResponseEntity.ok(body);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClassroomResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateClassroomRequest request) {
    Classroom classroom = updateClassroomUseCase.execute(ClassroomDtoMapper.toCommand(id, request));
    return ResponseEntity.ok(ClassroomDtoMapper.toResponse(classroom));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
  }
}
