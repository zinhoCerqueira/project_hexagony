package com.schoolqueue.infrastructure.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase.AnnounceArrivalCommand;
import com.schoolqueue.domain.ports.in.FetchActiveQueueUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.Cancel;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsArrived;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.MarkAsCompleted;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.QueueAction;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateQueueStatusCommand;
import com.schoolqueue.domain.ports.in.UpdateQueueStatusUseCase.UpdateRange;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PickupQueueController.class)
class PickupQueueControllerWebTest {

  private static final String SCHOOL_ID = "1b2f9c56-0a3e-4d8a-9c1b-5f6e7d8a9b01";
  private static final String STUDENT_ID = "c9bf9e57-1685-4c89-bafb-ff5af830be8a";
  private static final String PARENT_ID = "d3b07384-d113-424a-4f3b-2232938b2bb4";
  private static final String QUEUE_ITEM_ID = "0a1b2c3d-4e5f-6789-abcd-ef0123456789";

  @Autowired MockMvc mockMvc;

  @MockitoBean AnnounceArrivalUseCase announceArrivalUseCase;
  @MockitoBean UpdateQueueStatusUseCase updateQueueStatusUseCase;
  @MockitoBean FetchActiveQueueUseCase fetchActiveQueueUseCase;

  private PickupQueueItem itemWith(QueueStatus status, ProximityRange range, boolean called) {
    return PickupQueueItem.reconstitute(
        UUID.fromString(QUEUE_ITEM_ID),
        UUID.fromString(SCHOOL_ID),
        UUID.fromString(STUDENT_ID),
        UUID.fromString(PARENT_ID),
        status,
        called,
        range,
        new BigDecimal("-23.550520"),
        new BigDecimal("-46.633308"),
        Instant.parse("2026-08-27T12:00:00Z"),
        Instant.parse("2026-08-27T12:00:00Z"));
  }

  @Test
  @DisplayName("POST /announce returns 200 with the new queue item in EN_ROUTE")
  void shouldReturnCreatedItemInEnRouteOnAnnounce() throws Exception {
    when(announceArrivalUseCase.execute(any(AnnounceArrivalCommand.class)))
        .thenReturn(itemWith(QueueStatus.EN_ROUTE, ProximityRange.MEDIUM, false));

    mockMvc
        .perform(
            post("/api/v1/queue/announce")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "studentId": "%s",
                      "parentId": "%s",
                      "latitude": -23.550520,
                      "longitude": -46.633308
                    }
                    """
                        .formatted(SCHOOL_ID, STUDENT_ID, PARENT_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(QUEUE_ITEM_ID))
        .andExpect(jsonPath("$.schoolId").value(SCHOOL_ID))
        .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
        .andExpect(jsonPath("$.parentId").value(PARENT_ID))
        .andExpect(jsonPath("$.journeyStatus").value("EN_ROUTE"))
        .andExpect(jsonPath("$.called").value(false))
        .andExpect(jsonPath("$.currentRange").value("MEDIUM"))
        .andExpect(jsonPath("$.latitude").value(-23.550520))
        .andExpect(jsonPath("$.longitude").value(-46.633308));

    ArgumentCaptor<AnnounceArrivalCommand> captor =
        ArgumentCaptor.forClass(AnnounceArrivalCommand.class);
    verify(announceArrivalUseCase).execute(captor.capture());
    assertThat(captor.getValue().schoolId()).isEqualTo(UUID.fromString(SCHOOL_ID));
    assertThat(captor.getValue().latitude()).isEqualByComparingTo(new BigDecimal("-23.550520"));
    assertThat(captor.getValue().longitude()).isEqualByComparingTo(new BigDecimal("-46.633308"));
  }

  @Test
  @DisplayName("POST /announce returns 400 when latitude is missing")
  void shouldReturnBadRequestWhenLatitudeIsMissingOnAnnounce() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/queue/announce")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "studentId": "%s",
                      "parentId": "%s",
                      "longitude": -46.633308
                    }
                    """
                        .formatted(SCHOOL_ID, STUDENT_ID, PARENT_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='latitude')]").exists());

    verifyNoInteractions(announceArrivalUseCase);
  }

  @Test
  @DisplayName("POST /announce returns 400 when longitude is missing")
  void shouldReturnBadRequestWhenLongitudeIsMissingOnAnnounce() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/queue/announce")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "studentId": "%s",
                      "parentId": "%s",
                      "latitude": -23.550520
                    }
                    """
                        .formatted(SCHOOL_ID, STUDENT_ID, PARENT_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='longitude')]").exists());

    verifyNoInteractions(announceArrivalUseCase);
  }

  @Test
  @DisplayName(
      "POST /announce returns 400 when service throws IllegalStateException (e.g. duplicate)")
  void shouldReturnBadRequestWhenServiceThrowsIllegalState() throws Exception {
    when(announceArrivalUseCase.execute(any(AnnounceArrivalCommand.class)))
        .thenThrow(new IllegalStateException("Já existe um aviso de saída ativo para este aluno."));

    mockMvc
        .perform(
            post("/api/v1/queue/announce")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "studentId": "%s",
                      "parentId": "%s",
                      "latitude": -23.550520,
                      "longitude": -46.633308
                    }
                    """
                        .formatted(SCHOOL_ID, STUDENT_ID, PARENT_ID)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("state"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Já existe um aviso de saída ativo para este aluno."));
  }

  @Test
  @DisplayName("POST /announce returns 409 when service throws InvalidQueueStateException")
  void shouldReturnConflictWhenServiceThrowsInvalidQueueState() throws Exception {
    when(announceArrivalUseCase.execute(any(AnnounceArrivalCommand.class)))
        .thenThrow(new InvalidQueueStateException("Fila já finalizada ou cancelada"));

    mockMvc
        .perform(
            post("/api/v1/queue/announce")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "schoolId": "%s",
                      "studentId": "%s",
                      "parentId": "%s",
                      "latitude": -23.550520,
                      "longitude": -46.633308
                    }
                    """
                        .formatted(SCHOOL_ID, STUDENT_ID, PARENT_ID)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].field").value("state"))
        .andExpect(jsonPath("$.errors[0].message").value("Fila já finalizada ou cancelada"));
  }

  @Test
  @DisplayName("PATCH /{id}/status MARK_AS_COMPLETED returns 200 and forwards the action")
  void shouldForwardMarkAsCompletedActionOnStatusUpdate() throws Exception {
    when(updateQueueStatusUseCase.execute(any(UpdateQueueStatusCommand.class)))
        .thenReturn(itemWith(QueueStatus.COMPLETED, ProximityRange.CLOSE, true));

    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"MARK_AS_COMPLETED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(QUEUE_ITEM_ID))
        .andExpect(jsonPath("$.journeyStatus").value("COMPLETED"))
        .andExpect(jsonPath("$.called").value(true));

    ArgumentCaptor<UpdateQueueStatusCommand> captor =
        ArgumentCaptor.forClass(UpdateQueueStatusCommand.class);
    verify(updateQueueStatusUseCase).execute(captor.capture());
    assertThat(captor.getValue().queueItemId()).isEqualTo(UUID.fromString(QUEUE_ITEM_ID));
    assertThat(captor.getValue().action()).isInstanceOf(MarkAsCompleted.class);
  }

  @Test
  @DisplayName("PATCH /{id}/status UPDATE_RANGE forwards the new range to the use case")
  void shouldForwardUpdateRangeActionOnStatusUpdate() throws Exception {
    when(updateQueueStatusUseCase.execute(any(UpdateQueueStatusCommand.class)))
        .thenReturn(itemWith(QueueStatus.EN_ROUTE, ProximityRange.CLOSE, true));

    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"UPDATE_RANGE\", \"newRange\": \"CLOSE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentRange").value("CLOSE"))
        .andExpect(jsonPath("$.called").value(true));

    ArgumentCaptor<UpdateQueueStatusCommand> captor =
        ArgumentCaptor.forClass(UpdateQueueStatusCommand.class);
    verify(updateQueueStatusUseCase).execute(captor.capture());
    QueueAction action = captor.getValue().action();
    assertThat(action).isInstanceOf(UpdateRange.class);
    assertThat(((UpdateRange) action).newRange()).isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("PATCH /{id}/status MARK_AS_ARRIVED returns 200 and forwards the action")
  void shouldForwardMarkAsArrivedActionOnStatusUpdate() throws Exception {
    when(updateQueueStatusUseCase.execute(any(UpdateQueueStatusCommand.class)))
        .thenReturn(itemWith(QueueStatus.ARRIVED, ProximityRange.CLOSE, true));

    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"MARK_AS_ARRIVED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.journeyStatus").value("ARRIVED"));

    ArgumentCaptor<UpdateQueueStatusCommand> captor =
        ArgumentCaptor.forClass(UpdateQueueStatusCommand.class);
    verify(updateQueueStatusUseCase).execute(captor.capture());
    assertThat(captor.getValue().action()).isInstanceOf(MarkAsArrived.class);
  }

  @Test
  @DisplayName("PATCH /{id}/status CANCEL returns 200 and forwards the action")
  void shouldForwardCancelActionOnStatusUpdate() throws Exception {
    when(updateQueueStatusUseCase.execute(any(UpdateQueueStatusCommand.class)))
        .thenReturn(itemWith(QueueStatus.CANCELLED, ProximityRange.MEDIUM, false));

    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"CANCEL\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.journeyStatus").value("CANCELLED"));

    ArgumentCaptor<UpdateQueueStatusCommand> captor =
        ArgumentCaptor.forClass(UpdateQueueStatusCommand.class);
    verify(updateQueueStatusUseCase).execute(captor.capture());
    assertThat(captor.getValue().action()).isInstanceOf(Cancel.class);
  }

  @Test
  @DisplayName("PATCH /{id}/status with unknown action returns 400")
  void shouldReturnBadRequestWhenActionIsUnknown() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"DO_THE_THING\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("state"))
        .andExpect(jsonPath("$.errors[0].message").value("Unknown action: DO_THE_THING"));

    verifyNoInteractions(updateQueueStatusUseCase);
  }

  @Test
  @DisplayName("PATCH /{id}/status UPDATE_RANGE without newRange returns 400")
  void shouldReturnBadRequestWhenUpdateRangeHasNoNewRange() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"UPDATE_RANGE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("state"))
        .andExpect(jsonPath("$.errors[0].message").value("newRange is required for UPDATE_RANGE"));

    verifyNoInteractions(updateQueueStatusUseCase);
  }

  @Test
  @DisplayName("PATCH /{id}/status with blank action returns 400 from bean validation")
  void shouldReturnBadRequestWhenActionIsBlank() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='action')]").exists());

    verifyNoInteractions(updateQueueStatusUseCase);
  }

  @Test
  @DisplayName("GET /school/{schoolId}/active returns 200 with the queue list")
  void shouldReturnActiveQueueForSchool() throws Exception {
    when(fetchActiveQueueUseCase.execute(UUID.fromString(SCHOOL_ID)))
        .thenReturn(
            List.of(
                itemWith(QueueStatus.EN_ROUTE, ProximityRange.FAR, false),
                itemWith(QueueStatus.ARRIVED, ProximityRange.CLOSE, true)));

    mockMvc
        .perform(get("/api/v1/queue/school/{schoolId}/active", SCHOOL_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].schoolId").value(SCHOOL_ID))
        .andExpect(jsonPath("$[0].journeyStatus").value("EN_ROUTE"))
        .andExpect(jsonPath("$[0].currentRange").value("FAR"))
        .andExpect(jsonPath("$[1].journeyStatus").value("ARRIVED"))
        .andExpect(jsonPath("$[1].currentRange").value("CLOSE"))
        .andExpect(jsonPath("$[1].called").value(true));
  }

  @Test
  @DisplayName(
      "GET /school/{schoolId}/active returns 200 with empty list when there is no active item")
  void shouldReturnEmptyListWhenNoActiveItem() throws Exception {
    when(fetchActiveQueueUseCase.execute(UUID.fromString(SCHOOL_ID))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/queue/school/{schoolId}/active", SCHOOL_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("PATCH /{id}/status returns 409 when service throws InvalidQueueStateException")
  void shouldReturnConflictOnPatchWhenServiceThrowsInvalidQueueState() throws Exception {
    when(updateQueueStatusUseCase.execute(any(UpdateQueueStatusCommand.class)))
        .thenThrow(new InvalidQueueStateException("Aluno não pode ser entregue sem ter sido chamado"));

    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"MARK_AS_COMPLETED\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].field").value("state"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value("Aluno não pode ser entregue sem ter sido chamado"));
  }

  @Test
  @DisplayName("PATCH /{id}/status does not invoke the use case when bean validation fails")
  void shouldNotInvokeUseCaseWhenPatchFailsBeanValidation() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/queue/{id}/status", QUEUE_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());

    verify(updateQueueStatusUseCase, never()).execute(any(UpdateQueueStatusCommand.class));
  }
}
