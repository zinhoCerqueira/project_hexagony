package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterSchoolRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.SchoolResponse;
import com.schoolqueue.infrastructure.adapters.in.web.mapper.SchoolDtoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

  private final RegisterSchoolUseCase registerSchoolUseCase;

  public SchoolController(RegisterSchoolUseCase registerSchoolUseCase) {
    this.registerSchoolUseCase = registerSchoolUseCase;
  }

  @PostMapping
  public ResponseEntity<SchoolResponse> register(
      @Valid @RequestBody RegisterSchoolRequest request) {
    School school = registerSchoolUseCase.execute(SchoolDtoMapper.toCommand(request));
    URI location = URI.create("/api/v1/schools/" + school.id());
    return ResponseEntity.created(location).body(SchoolDtoMapper.toResponse(school));
  }
}
