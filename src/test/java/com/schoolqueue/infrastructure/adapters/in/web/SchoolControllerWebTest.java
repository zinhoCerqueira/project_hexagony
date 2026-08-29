package com.schoolqueue.infrastructure.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.FetchSchoolUseCase;
import com.schoolqueue.domain.ports.in.ListSchoolsUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase.RegisterSchoolCommand;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase.UpdateSchoolCommand;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SchoolController.class)
class SchoolControllerWebTest {

  private static final String SCHOOL_ID = "1b2f9c56-0a3e-4d8a-9c1b-5f6e7d8a9b01";

  @Autowired MockMvc mockMvc;

  @MockitoBean RegisterSchoolUseCase registerSchoolUseCase;
  @MockitoBean FetchSchoolUseCase fetchSchoolUseCase;
  @MockitoBean ListSchoolsUseCase listSchoolsUseCase;
  @MockitoBean UpdateSchoolUseCase updateSchoolUseCase;

  private School schoolWithGps() {
    return new School(
        UUID.fromString(SCHOOL_ID),
        "Escola Exemplo",
        new BigDecimal("-23.550520"),
        new BigDecimal("-46.633308"));
  }

  private void stubSaveReturnsSchool() {
    when(registerSchoolUseCase.execute(any(RegisterSchoolCommand.class)))
        .thenReturn(schoolWithGps());
  }

  @Test
  @DisplayName("POST returns 201 with the created school and Location header")
  void shouldReturnCreatedSchoolWithLocationHeader() throws Exception {
    stubSaveReturnsSchool();

    mockMvc
        .perform(
            post("/api/v1/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Escola Exemplo",
                      "latitude": -23.550520,
                      "longitude": -46.633308
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/schools/" + SCHOOL_ID))
        .andExpect(jsonPath("$.id").value(SCHOOL_ID))
        .andExpect(jsonPath("$.name").value("Escola Exemplo"))
        .andExpect(jsonPath("$.latitude").value(-23.550520))
        .andExpect(jsonPath("$.longitude").value(-46.633308));

    ArgumentCaptor<RegisterSchoolCommand> captor =
        ArgumentCaptor.forClass(RegisterSchoolCommand.class);
    verify(registerSchoolUseCase).execute(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Escola Exemplo");
    assertThat(captor.getValue().latitude()).isEqualByComparingTo(new BigDecimal("-23.550520"));
    assertThat(captor.getValue().longitude()).isEqualByComparingTo(new BigDecimal("-46.633308"));
  }

  @Test
  @DisplayName("POST returns 400 when latitude is missing")
  void shouldReturnBadRequestWhenLatitudeIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Escola Exemplo",
                      "longitude": -46.633308
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='latitude')]").exists());

    verifyNoInteractions(registerSchoolUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when longitude is missing")
  void shouldReturnBadRequestWhenLongitudeIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Escola Exemplo",
                      "latitude": -23.550520
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='longitude')]").exists());

    verifyNoInteractions(registerSchoolUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when payload has no GPS coordinates at all")
  void shouldReturnBadRequestWhenPayloadHasNoGpsCoordinates() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Escola Exemplo"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(2)));

    verifyNoInteractions(registerSchoolUseCase);
  }

  @Test
  @DisplayName("POST returns 400 when name is blank")
  void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "   ",
                      "latitude": -23.550520,
                      "longitude": -46.633308
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='name')]").exists());

    verifyNoInteractions(registerSchoolUseCase);
  }

  @Test
  @DisplayName("GET returns 200 with the list of schools")
  void shouldListSchools() throws Exception {
    School other = new School(UUID.randomUUID(), "Outra", new BigDecimal("0"), new BigDecimal("0"));
    when(listSchoolsUseCase.execute()).thenReturn(List.of(schoolWithGps(), other));

    mockMvc
        .perform(get("/api/v1/schools"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(SCHOOL_ID))
        .andExpect(jsonPath("$[0].name").value("Escola Exemplo"))
        .andExpect(jsonPath("$[1].name").value("Outra"));
  }

  @Test
  @DisplayName("GET /{id} returns 200 with the school when it exists")
  void shouldFetchSchoolById() throws Exception {
    when(fetchSchoolUseCase.execute(UUID.fromString(SCHOOL_ID))).thenReturn(schoolWithGps());

    mockMvc
        .perform(get("/api/v1/schools/" + SCHOOL_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(SCHOOL_ID))
        .andExpect(jsonPath("$.name").value("Escola Exemplo"));
  }

  @Test
  @DisplayName("GET /{id} returns 404 when the school does not exist")
  void shouldReturnNotFoundWhenFetchingMissingSchool() throws Exception {
    UUID missing = UUID.randomUUID();
    when(fetchSchoolUseCase.execute(missing))
        .thenThrow(new SchoolNotFoundException("Escola não encontrada"));

    mockMvc
        .perform(get("/api/v1/schools/" + missing))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].field").value("schoolId"));
  }

  @Test
  @DisplayName("PUT /{id} returns 200 with the updated school")
  void shouldUpdateSchool() throws Exception {
    School updated =
        new School(
            UUID.fromString(SCHOOL_ID),
            "Escola Renomeada",
            new BigDecimal("-22.0"),
            new BigDecimal("-45.0"));
    when(updateSchoolUseCase.execute(any(UpdateSchoolCommand.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/v1/schools/" + SCHOOL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Escola Renomeada",
                      "latitude": -22.0,
                      "longitude": -45.0
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Escola Renomeada"));

    ArgumentCaptor<UpdateSchoolCommand> captor =
        ArgumentCaptor.forClass(UpdateSchoolCommand.class);
    verify(updateSchoolUseCase).execute(captor.capture());
    assertThat(captor.getValue().id()).isEqualTo(UUID.fromString(SCHOOL_ID));
  }

  @Test
  @DisplayName("PUT /{id} returns 404 when the school does not exist")
  void shouldReturnNotFoundWhenUpdatingMissingSchool() throws Exception {
    UUID missing = UUID.randomUUID();
    when(updateSchoolUseCase.execute(any(UpdateSchoolCommand.class)))
        .thenThrow(new SchoolNotFoundException("Escola não encontrada"));

    mockMvc
        .perform(
            put("/api/v1/schools/" + missing)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "X",
                      "latitude": 0,
                      "longitude": 0
                    }
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].field").value("schoolId"));
  }

  @Test
  @DisplayName("PUT /{id} returns 400 when name is blank")
  void shouldReturnBadRequestWhenUpdatingWithBlankName() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/schools/" + SCHOOL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "  ",
                      "latitude": 0,
                      "longitude": 0
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='name')]").exists());

    verifyNoInteractions(updateSchoolUseCase);
  }

  @Test
  @DisplayName("DELETE /{id} returns 405 Method Not Allowed (LAC20)")
  void shouldReturnMethodNotAllowedOnDelete() throws Exception {
    mockMvc.perform(delete("/api/v1/schools/" + SCHOOL_ID)).andExpect(status().isMethodNotAllowed());
  }
}
