package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.FetchSchoolUseCase;
import com.schoolqueue.domain.ports.in.ListSchoolsUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterSchoolRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.SchoolResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateSchoolRequest;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.SchoolDtoMapper;
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
@RequestMapping("/api/v1/schools")
public class SchoolController {

  private final RegisterSchoolUseCase registerSchoolUseCase;
  private final FetchSchoolUseCase fetchSchoolUseCase;
  private final ListSchoolsUseCase listSchoolsUseCase;
  private final UpdateSchoolUseCase updateSchoolUseCase;

  public SchoolController(
      RegisterSchoolUseCase registerSchoolUseCase,
      FetchSchoolUseCase fetchSchoolUseCase,
      ListSchoolsUseCase listSchoolsUseCase,
      UpdateSchoolUseCase updateSchoolUseCase) {
    this.registerSchoolUseCase = registerSchoolUseCase;
    this.fetchSchoolUseCase = fetchSchoolUseCase;
    this.listSchoolsUseCase = listSchoolsUseCase;
    this.updateSchoolUseCase = updateSchoolUseCase;
  }

  @PostMapping
  public ResponseEntity<SchoolResponse> register(@Valid @RequestBody RegisterSchoolRequest request) {
    School school = registerSchoolUseCase.execute(SchoolDtoMapper.toCommand(request));
    URI location = URI.create("/api/v1/schools/" + school.id());
    return ResponseEntity.created(location).body(SchoolDtoMapper.toResponse(school));
  }

  @GetMapping
  public ResponseEntity<List<SchoolResponse>> list() {
    List<SchoolResponse> body =
        listSchoolsUseCase.execute().stream().map(SchoolDtoMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SchoolResponse> fetch(@PathVariable UUID id) {
    School school = fetchSchoolUseCase.execute(id);
    return ResponseEntity.ok(SchoolDtoMapper.toResponse(school));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SchoolResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateSchoolRequest request) {
    School school = updateSchoolUseCase.execute(SchoolDtoMapper.toCommand(id, request));
    return ResponseEntity.ok(SchoolDtoMapper.toResponse(school));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
  }
}
