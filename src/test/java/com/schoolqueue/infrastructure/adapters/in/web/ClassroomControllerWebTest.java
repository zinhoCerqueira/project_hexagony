package com.schoolqueue.infrastructure.adapters.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.FetchClassroomUseCase;
import com.schoolqueue.domain.ports.in.ListClassroomsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase.RegisterClassroomCommand;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClassroomController.class)
class ClassroomControllerWebTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean RegisterClassroomUseCase registerClassroomUseCase;
  @MockitoBean FetchClassroomUseCase fetchClassroomUseCase;
  @MockitoBean ListClassroomsBySchoolUseCase listClassroomsBySchoolUseCase;
  @MockitoBean UpdateClassroomUseCase updateClassroomUseCase;

  private Classroom newClassroom(UUID schoolId) {
    return new Classroom(UUID.randomUUID(), schoolId, "Turma A");
  }

  @Test
  @DisplayName("POST returns 201 with the created classroom and Location header")
  void shouldCreateClassroom() throws Exception {
    UUID schoolId = UUID.randomUUID();
    when(registerClassroomUseCase.execute(any(RegisterClassroomCommand.class)))
        .thenReturn(newClassroom(schoolId));

    mockMvc
        .perform(
            post("/api/v1/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "name": "Turma A"
                    }
                    """
                        .formatted(schoolId)))
        .andExpect(status().isCreated())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(jsonPath("$.name").value("Turma A"));
  }

  @Test
  @DisplayName("POST returns 400 when name is blank")
  void shouldReturn400WhenNameIsBlank() throws Exception {
    UUID schoolId = UUID.randomUUID();
    mockMvc
        .perform(
            post("/api/v1/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "name": "  "
                    }
                    """
                        .formatted(schoolId)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerClassroomUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when schoolId is missing")
  void shouldReturn400WhenSchoolIdIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Turma A\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerClassroomUseCase);
  }

  @Test
  @DisplayName("POST returns 404 when the school does not exist")
  void shouldReturn404WhenSchoolDoesNotExist() throws Exception {
    UUID schoolId = UUID.randomUUID();
    when(registerClassroomUseCase.execute(any(RegisterClassroomCommand.class)))
        .thenThrow(new SchoolNotFoundException("Escola não encontrada"));

    mockMvc
        .perform(
            post("/api/v1/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "name": "Turma A"
                    }
                    """
                        .formatted(schoolId)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /{id} returns 200 with the classroom when it exists")
  void shouldFetchClassroomById() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchClassroomUseCase.execute(id)).thenReturn(new Classroom(id, UUID.randomUUID(), "X"));

    mockMvc
        .perform(get("/api/v1/classrooms/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("GET /{id} returns 404 when the classroom does not exist")
  void shouldReturn404WhenClassroomMissing() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchClassroomUseCase.execute(id))
        .thenThrow(new ClassroomNotFoundException("Turma não encontrada"));

    mockMvc
        .perform(get("/api/v1/classrooms/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].field").value("classroomId"));
  }

  @Test
  @DisplayName("GET /school/{schoolId} returns 200 with the list of classrooms")
  void shouldListClassroomsBySchool() throws Exception {
    UUID schoolId = UUID.randomUUID();
    when(listClassroomsBySchoolUseCase.execute(schoolId))
        .thenReturn(List.of(newClassroom(schoolId), newClassroom(schoolId)));

    mockMvc
        .perform(get("/api/v1/classrooms/school/" + schoolId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("PUT /{id} returns 200 with the updated classroom")
  void shouldUpdateClassroom() throws Exception {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    Classroom updated = new Classroom(id, schoolId, "New");
    when(updateClassroomUseCase.execute(any())).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/v1/classrooms/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "name": "New"
                    }
                    """
                        .formatted(schoolId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New"));
  }

  @Test
  @DisplayName("PUT /{id} returns 404 when the classroom does not exist")
  void shouldReturn404WhenUpdatingMissing() throws Exception {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    when(updateClassroomUseCase.execute(any()))
        .thenThrow(new ClassroomNotFoundException("Turma não encontrada"));

    mockMvc
        .perform(
            put("/api/v1/classrooms/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "name": "X"
                    }
                    """
                        .formatted(schoolId)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /{id} returns 405 (LAC20)")
  void shouldReturnMethodNotAllowedOnDelete() throws Exception {
    mockMvc.perform(delete("/api/v1/classrooms/" + UUID.randomUUID()))
        .andExpect(status().isMethodNotAllowed());
    verifyNoInteractions(registerClassroomUseCase, fetchClassroomUseCase,
        listClassroomsBySchoolUseCase, updateClassroomUseCase);
  }
}
