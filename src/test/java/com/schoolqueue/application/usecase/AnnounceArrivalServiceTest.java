package com.schoolqueue.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase.AnnounceArrivalCommand;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnounceArrivalServiceTest {

  private static final BigDecimal SCHOOL_LAT = new BigDecimal("-23.550520");
  private static final BigDecimal SCHOOL_LNG = new BigDecimal("-46.633308");
  private static final BigDecimal PARENT_LAT_CLOSE = new BigDecimal("-23.554120");
  private static final BigDecimal PARENT_LAT_MEDIUM = new BigDecimal("-23.559520");
  private static final BigDecimal PARENT_LAT_FAR = new BigDecimal("-23.577520");

  @Mock QueueRepositoryPort queueRepositoryPort;

  @Mock QueueNotificationPort notificationPort;

  @Mock SchoolRepositoryPort schoolRepositoryPort;

  private AnnounceArrivalCommand newCommand(BigDecimal latitude, BigDecimal longitude) {
    return new AnnounceArrivalCommand(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), latitude, longitude);
  }

  private School schoolWithGps() {
    return new School(UUID.randomUUID(), "Escola Municipal", SCHOOL_LAT, SCHOOL_LNG);
  }

  private void stubSchoolFound(AnnounceArrivalCommand command, School school) {
    when(schoolRepositoryPort.findById(command.schoolId())).thenReturn(Optional.of(school));
  }

  private void stubNoActiveItem(AnnounceArrivalCommand command) {
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(Optional.empty());
  }

  private void stubSaveReturnsSameItem() {
    when(queueRepositoryPort.save(any(PickupQueueItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private AnnounceArrivalService newService() {
    return new AnnounceArrivalService(queueRepositoryPort, notificationPort, schoolRepositoryPort);
  }

  @Test
  @DisplayName("saves and notifies a new EN_ROUTE item when no active item exists")
  void shouldSaveAndNotifyEnRouteItemWhenNoActiveItemExists() {
    AnnounceArrivalCommand command = newCommand(PARENT_LAT_MEDIUM, SCHOOL_LNG);
    stubNoActiveItem(command);
    stubSaveReturnsSameItem();
    stubSchoolFound(command, schoolWithGps());

    PickupQueueItem result = newService().execute(command);

    ArgumentCaptor<PickupQueueItem> captor = ArgumentCaptor.forClass(PickupQueueItem.class);
    verify(queueRepositoryPort).save(captor.capture());

    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(result.called()).isFalse();
    assertThat(captor.getValue().schoolId()).isEqualTo(command.schoolId());
    assertThat(captor.getValue().studentId()).isEqualTo(command.studentId());
    assertThat(captor.getValue().parentId()).isEqualTo(command.parentId());
    assertThat(captor.getValue().currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(captor.getValue().latitude()).isEqualByComparingTo(PARENT_LAT_MEDIUM);
    assertThat(captor.getValue().longitude()).isEqualByComparingTo(SCHOOL_LNG);
    verify(notificationPort).notifyStudentArrivalAnnounced(result);
  }

  @Test
  @DisplayName("throws IllegalStateException when the student already has an active item")
  void shouldThrowIllegalStateExceptionWhenStudentAlreadyHasActiveItem() {
    AnnounceArrivalCommand command = newCommand(PARENT_LAT_MEDIUM, SCHOOL_LNG);
    when(queueRepositoryPort.findActiveByStudentId(command.studentId()))
        .thenReturn(
            Optional.of(
                new PickupQueueItem(
                    null,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    ProximityRange.FAR)));

    assertThatThrownBy(() -> newService().execute(command))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Já existe um aviso de saída ativo para este aluno.");

    verify(schoolRepositoryPort, never()).findById(any(UUID.class));
    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }

  @Test
  @DisplayName("starts as called when the parent is within the CLOSE range")
  void shouldStartCalledWhenParentIsWithinCloseRange() {
    AnnounceArrivalCommand command = newCommand(PARENT_LAT_CLOSE, SCHOOL_LNG);
    stubNoActiveItem(command);
    stubSaveReturnsSameItem();
    stubSchoolFound(command, schoolWithGps());

    PickupQueueItem result = newService().execute(command);

    assertThat(result.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(result.called()).isTrue();
    assertThat(result.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
  }

  @Test
  @DisplayName("classifies a distant parent as FAR without calling the student")
  void shouldClassifyDistantParentAsFarWithoutCalling() {
    AnnounceArrivalCommand command = newCommand(PARENT_LAT_FAR, SCHOOL_LNG);
    stubNoActiveItem(command);
    stubSaveReturnsSameItem();
    stubSchoolFound(command, schoolWithGps());

    PickupQueueItem result = newService().execute(command);

    assertThat(result.currentRange()).isEqualTo(ProximityRange.FAR);
    assertThat(result.called()).isFalse();
  }

  @Test
  @DisplayName("throws SchoolNotFoundException when the school does not exist")
  void shouldThrowSchoolNotFoundExceptionWhenSchoolDoesNotExist() {
    AnnounceArrivalCommand command = newCommand(PARENT_LAT_MEDIUM, SCHOOL_LNG);
    stubNoActiveItem(command);
    when(schoolRepositoryPort.findById(command.schoolId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> newService().execute(command))
        .isInstanceOf(SchoolNotFoundException.class)
        .hasMessage("Escola não encontrada");

    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }

  @Test
  @DisplayName("throws IllegalArgumentException when the parent GPS is missing")
  void shouldThrowIllegalArgumentExceptionWhenParentGpsIsMissing() {
    AnnounceArrivalCommand command = newCommand(null, null);
    stubNoActiveItem(command);

    assertThatThrownBy(() -> newService().execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Latitude and longitude must not be null");

    verify(schoolRepositoryPort, never()).findById(any(UUID.class));
    verify(queueRepositoryPort, never()).save(any(PickupQueueItem.class));
    verifyNoInteractions(notificationPort);
  }
}
