package com.schoolqueue.infrastructure.adapters.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.schoolqueue.domain.exception.StudentNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.FetchStudentUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsByClassroomUseCase;
import com.schoolqueue.domain.ports.in.ListStudentsBySchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase;
import com.schoolqueue.domain.ports.in.UpdateStudentUseCase;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
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

@WebMvcTest(StudentController.class)
class StudentControllerWebTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean RegisterStudentUseCase registerStudentUseCase;
  @MockitoBean FetchStudentUseCase fetchStudentUseCase;
  @MockitoBean ListStudentsBySchoolUseCase listStudentsBySchoolUseCase;
  @MockitoBean ListStudentsByClassroomUseCase listStudentsByClassroomUseCase;
  @MockitoBean UpdateStudentUseCase updateStudentUseCase;
  @MockitoBean ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort;

  @Test
  @DisplayName("POST returns 201 with the created student and parentIds")
  void shouldCreateStudent() throws Exception {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Student student = new Student(UUID.randomUUID(), schoolId, classroomId, "João");
    when(registerStudentUseCase.execute(any())).thenReturn(student);
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(student.id()))
        .thenReturn(List.of(parentId));

    mockMvc
        .perform(
            post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "classroomId": "%s",
                      "name": "João",
                      "parentIds": ["%s"]
                    }
                    """
                        .formatted(schoolId, classroomId, parentId)))
        .andExpect(status().isCreated())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(jsonPath("$.name").value("João"))
        .andExpect(jsonPath("$.parentIds[0]").value(parentId.toString()));
  }

  @Test
  @DisplayName("POST returns 400 when parentIds is empty")
  void shouldReturn400WhenParentIdsEmpty() throws Exception {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    mockMvc
        .perform(
            post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "classroomId": "%s",
                      "name": "João",
                      "parentIds": []
                    }
                    """
                        .formatted(schoolId, classroomId)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerStudentUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when name is blank")
  void shouldReturn400WhenNameBlank() throws Exception {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    mockMvc
        .perform(
            post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "classroomId": "%s",
                      "name": "",
                      "parentIds": ["%s"]
                    }
                    """
                        .formatted(schoolId, classroomId, parentId)))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerStudentUseCase);
  }

  @Test
  @DisplayName("GET /{id} returns 200 with the student when it exists")
  void shouldFetchStudent() throws Exception {
    UUID id = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Student student = new Student(id, UUID.randomUUID(), UUID.randomUUID(), "X");
    when(fetchStudentUseCase.execute(id)).thenReturn(student);
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(id)).thenReturn(List.of(parentId));

    mockMvc
        .perform(get("/api/v1/students/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.parentIds[0]").value(parentId.toString()));
  }

  @Test
  @DisplayName("GET /{id} returns 404 when the student does not exist")
  void shouldReturn404WhenMissing() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchStudentUseCase.execute(id))
        .thenThrow(new StudentNotFoundException("Aluno não encontrado"));

    mockMvc
        .perform(get("/api/v1/students/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].field").value("studentId"));
  }

  @Test
  @DisplayName("GET /school/{schoolId} returns 200 with the list of students")
  void shouldListBySchool() throws Exception {
    UUID schoolId = UUID.randomUUID();
    Student a = new Student(UUID.randomUUID(), schoolId, UUID.randomUUID(), "A");
    Student b = new Student(UUID.randomUUID(), schoolId, UUID.randomUUID(), "B");
    when(listStudentsBySchoolUseCase.execute(schoolId)).thenReturn(List.of(a, b));
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(a.id())).thenReturn(List.of());
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(b.id())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/students/school/" + schoolId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("GET /classroom/{classroomId} returns 200 with the list of students")
  void shouldListByClassroom() throws Exception {
    UUID classroomId = UUID.randomUUID();
    Student a = new Student(UUID.randomUUID(), UUID.randomUUID(), classroomId, "A");
    when(listStudentsByClassroomUseCase.execute(classroomId)).thenReturn(List.of(a));
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(a.id())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/students/classroom/" + classroomId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @DisplayName("PUT /{id} returns 200 with the updated student")
  void shouldUpdateStudent() throws Exception {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Student updated = new Student(id, schoolId, classroomId, "Updated");
    when(updateStudentUseCase.execute(any())).thenReturn(updated);
    when(parentStudentLinkRepositoryPort.findParentsOfStudent(id)).thenReturn(List.of(parentId));

    mockMvc
        .perform(
            put("/api/v1/students/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "classroomId": "%s",
                      "name": "Updated",
                      "parentIds": ["%s"]
                    }
                    """
                        .formatted(schoolId, classroomId, parentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  @DisplayName("PUT /{id} returns 404 when the student does not exist")
  void shouldReturn404WhenUpdatingMissing() throws Exception {
    UUID id = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    when(updateStudentUseCase.execute(any()))
        .thenThrow(new StudentNotFoundException("Aluno não encontrado"));

    mockMvc
        .perform(
            put("/api/v1/students/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "classroomId": "%s",
                      "name": "X",
                      "parentIds": ["%s"]
                    }
                    """
                        .formatted(schoolId, classroomId, parentId)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /{id} returns 405 (LAC20)")
  void shouldReturnMethodNotAllowedOnDelete() throws Exception {
    mockMvc.perform(delete("/api/v1/students/" + UUID.randomUUID()))
        .andExpect(status().isMethodNotAllowed());
  }
}
