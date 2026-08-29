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

  @Test
  @DisplayName("POST returns 201 with the created parent")
  void shouldCreateParent() throws Exception {
    when(registerParentUseCase.execute(any()))
        .thenReturn(new Parent(UUID.randomUUID(), "Maria", "11999998888"));

    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Maria\",\"phone\":\"11999998888\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists(HttpHeaders.LOCATION))
        .andExpect(jsonPath("$.name").value("Maria"));
  }

  @Test
  @DisplayName("POST returns 400 when name is blank")
  void shouldReturn400WhenNameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/parents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  \",\"phone\":\"11999998888\"}"))
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
                .content("{\"name\":\"Maria\",\"phone\":\"\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registerParentUseCase);
  }

  @Test
  @DisplayName("GET returns 200 with the list of parents")
  void shouldListParents() throws Exception {
    when(listParentsUseCase.execute())
        .thenReturn(
            List.of(
                new Parent(UUID.randomUUID(), "A", "1"),
                new Parent(UUID.randomUUID(), "B", "2")));

    mockMvc
        .perform(get("/api/v1/parents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @DisplayName("GET /{id} returns 200 with the parent when it exists")
  void shouldFetchParent() throws Exception {
    UUID id = UUID.randomUUID();
    when(fetchParentUseCase.execute(id)).thenReturn(new Parent(id, "Maria", "1"));

    mockMvc
        .perform(get("/api/v1/parents/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
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
        .thenReturn(new Parent(id, "New", "999"));

    mockMvc
        .perform(
            put("/api/v1/parents/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\",\"phone\":\"999\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New"));
  }

  @Test
  @DisplayName("DELETE /{id} returns 405 (LAC20)")
  void shouldReturnMethodNotAllowedOnDelete() throws Exception {
    mockMvc.perform(delete("/api/v1/parents/" + UUID.randomUUID()))
        .andExpect(status().isMethodNotAllowed());
  }
}
