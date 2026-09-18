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

import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.FetchParentUseCase;
import com.schoolqueue.domain.ports.in.ListParentsUseCase;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase;
import com.schoolqueue.domain.ports.out.ParentSchoolLinkRepositoryPort;
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

@WebMvcTest(ParentController.class)
class ParentControllerWebTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean RegisterParentUseCase registerParentUseCase;
  @MockitoBean FetchParentUseCase fetchParentUseCase;
  @MockitoBean ListParentsUseCase listParentsUseCase;
  @MockitoBean UpdateParentUseCase updateParentUseCase;
  @MockitoBean ParentSchoolLinkRepositoryPort parentSchoolLinkRepositoryPort;

  @Test
  @DisplayName("POST returns 201 with the created parent")
  void shouldCreateParent() throws Exception {
    UUID schoolId = UUID.randomUUID();
    when(registerParentUseCase.execute(any()))
        .thenReturn(new Parent(UUID.randomUUID(), "Maria", "11999998888", "maria@mail.com"));
    when(parentSchoolLinkRepositoryPort.findSchoolsOfParent(any())).thenReturn(List.of(schoolId));

    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Maria\",\"phone\":\"11999998888\",\"email\":\"maria@mail.com\",\"schoolId\":\""
                        + schoolId
                        + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(jsonPath("$.name").value("Maria"))
        .andExpect(jsonPath("$.email").value("maria@mail.com"))
        .andExpect(jsonPath("$.schoolIds", hasSize(1)));
  }

  @Test
  @DisplayName("POST returns 400 when name is blank")
  void shouldReturn400WhenNameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"  \",\"phone\":\"11999998888\",\"email\":\"maria@mail.com\",\"schoolId\":\""
                        + UUID.randomUUID()
                        + "\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerParentUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when phone is blank")
  void shouldReturn400WhenPhoneIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Maria\",\"phone\":\"\",\"email\":\"maria@mail.com\",\"schoolId\":\""
                        + UUID.randomUUID()
                        + "\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerParentUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when email is invalid")
  void shouldReturn400WhenEmailIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Maria\",\"phone\":\"11999998888\",\"email\":\"not-an-email\",\"schoolId\":\""
                        + UUID.randomUUID()
                        + "\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerParentUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when schoolId is missing")
  void shouldReturn400WhenSchoolIdIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Maria\",\"phone\":\"11999998888\",\"email\":\"maria@mail.com\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerParentUseCase);
  }

  @Test
  @DisplayName("GET returns 200 with the list of parents")
  void shouldListParents() throws Exception {
    when(listParentsUseCase.execute())
        .thenReturn(
            List.of(
                new Parent(UUID.randomUUID(), "A", "1", "a@mail.com"),
                new Parent(UUID.randomUUID(), "B", "2", "b@mail.com")));
    when(parentSchoolLinkRepositoryPort.findSchoolsOfParent(any())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/parents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("GET /{id} returns 200 with the parent when it exists")
  void shouldFetchParent() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchParentUseCase.execute(id)).thenReturn(new Parent(id, "Maria", "1", "maria@mail.com"));
    when(parentSchoolLinkRepositoryPort.findSchoolsOfParent(id))
        .thenReturn(List.of(UUID.randomUUID()));

    mockMvc
        .perform(get("/api/v1/parents/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.email").value("maria@mail.com"));
  }

  @Test
  @DisplayName("GET /{id} returns 404 when the parent does not exist")
  void shouldReturn404WhenMissing() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchParentUseCase.execute(id))
        .thenThrow(new ParentNotFoundException("Responsável não encontrado"));

    mockMvc
        .perform(get("/api/v1/parents/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].field").value("parentId"));
  }

  @Test
  @DisplayName("PUT /{id} returns 200 with the updated parent")
  void shouldUpdateParent() throws Exception {
    UUID id = UUID.randomUUID();
    when(updateParentUseCase.execute(any()))
        .thenReturn(new Parent(id, "New", "999", "new@mail.com"));
    when(parentSchoolLinkRepositoryPort.findSchoolsOfParent(id)).thenReturn(List.of());

    mockMvc
        .perform(
            put("/api/v1/parents/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\",\"phone\":\"999\",\"email\":\"new@mail.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New"))
        .andExpect(jsonPath("$.email").value("new@mail.com"));
  }

  @Test
  @DisplayName("PUT /{id} returns 400 when email is invalid")
  void shouldReturn400OnUpdateWhenEmailIsInvalid() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/parents/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\",\"phone\":\"999\",\"email\":\"not-an-email\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(updateParentUseCase);
  }

  @Test
  @DisplayName("DELETE /{id} returns 405 (LAC20)")
  void shouldReturnMethodNotAllowedOnDelete() throws Exception {
    mockMvc
        .perform(delete("/api/v1/parents/" + UUID.randomUUID()))
        .andExpect(status().isMethodNotAllowed());
  }
}
