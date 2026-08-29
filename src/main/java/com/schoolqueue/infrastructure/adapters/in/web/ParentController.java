package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.FetchParentUseCase;
import com.schoolqueue.domain.ports.in.ListParentsUseCase;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ParentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterParentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateParentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.ParentDtoMapper;
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
@RequestMapping("/api/v1/parents")
public class ParentController {

  private final RegisterParentUseCase registerParentUseCase;
  private final FetchParentUseCase fetchParentUseCase;
  private final ListParentsUseCase listParentsUseCase;
  private final UpdateParentUseCase updateParentUseCase;

  public ParentController(
      RegisterParentUseCase registerParentUseCase,
      FetchParentUseCase fetchParentUseCase,
      ListParentsUseCase listParentsUseCase,
      UpdateParentUseCase updateParentUseCase) {
    this.registerParentUseCase = registerParentUseCase;
    this.fetchParentUseCase = fetchParentUseCase;
    this.listParentsUseCase = listParentsUseCase;
    this.updateParentUseCase = updateParentUseCase;
  }

  @PostMapping
  public ResponseEntity<ParentResponse> register(@Valid @RequestBody RegisterParentRequest request) {
    Parent parent = registerParentUseCase.execute(ParentDtoMapper.toCommand(request));
    URI location = URI.create("/api/v1/parents/" + parent.id());
    return ResponseEntity.created(location).body(ParentDtoMapper.toResponse(parent));
  }

  @GetMapping
  public ResponseEntity<List<ParentResponse>> list() {
    List<ParentResponse> body =
        listParentsUseCase.execute().stream().map(ParentDtoMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ParentResponse> fetch(@PathVariable UUID id) {
    Parent parent = fetchParentUseCase.execute(id);
    return ResponseEntity.ok(ParentDtoMapper.toResponse(parent));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ParentResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateParentRequest request) {
    Parent parent = updateParentUseCase.execute(ParentDtoMapper.toCommand(id, request));
    return ResponseEntity.ok(ParentDtoMapper.toResponse(parent));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
  }
}
