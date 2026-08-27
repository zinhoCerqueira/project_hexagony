package com.schoolqueue.infrastructure.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase.RegisterSchoolCommand;
import java.math.BigDecimal;
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
  @DisplayName("returns 201 with the created school and Location header")
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
  @DisplayName("returns 400 when latitude is missing")
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
  @DisplayName("returns 400 when longitude is missing")
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
  @DisplayName("returns 400 when payload has no GPS coordinates at all")
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
  @DisplayName("returns 400 when name is blank")
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
}
