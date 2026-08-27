package com.schoolqueue.infrastructure.adapters.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.infrastructure.adapters.out.messaging.dto.ArrivalAnnouncedEvent;
import com.schoolqueue.infrastructure.adapters.out.messaging.dto.StatusChangedEvent;
import com.schoolqueue.infrastructure.config.RabbitMQConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMQNotificationAdapterTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @InjectMocks private RabbitMQNotificationAdapter adapter;

  private PickupQueueItem newItemWithStatus(QueueStatus status, boolean called) {
    return PickupQueueItem.reconstitute(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        status,
        called,
        ProximityRange.MEDIUM,
        new BigDecimal("-23.5505"),
        new BigDecimal("-46.6333"),
        Instant.parse("2026-08-27T12:00:00Z"),
        Instant.parse("2026-08-27T12:00:00Z"));
  }

  @Test
  @DisplayName(
      "notifyStudentArrivalAnnounced publishes to school.queue.events with queue.arrival.announced"
          + " routing key")
  void shouldPublishArrivalAnnouncedToConfiguredExchangeAndRoutingKey() {
    PickupQueueItem item = newItemWithStatus(QueueStatus.EN_ROUTE, false);
    Instant before = Instant.now();

    adapter.notifyStudentArrivalAnnounced(item);

    ArgumentCaptor<ArrivalAnnouncedEvent> captor =
        ArgumentCaptor.forClass(ArrivalAnnouncedEvent.class);
    verify(rabbitTemplate)
        .convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_ARRIVAL_ANNOUNCED),
            captor.capture());
    Instant after = Instant.now();

    ArrivalAnnouncedEvent event = captor.getValue();
    assertThat(event.queueItemId()).isEqualTo(item.id());
    assertThat(event.studentId()).isEqualTo(item.studentId());
    assertThat(event.schoolId()).isEqualTo(item.schoolId());
    assertThat(event.status()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(event.called()).isEqualTo(item.called());
    assertThat(event.currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(event.occurredAt()).isBetween(before, after);
  }

  @Test
  @DisplayName("notifyStudentArrivalAnnounced propagates AmqpException from the broker")
  void shouldPropagateAmqpExceptionWhenBrokerFailsOnArrival() {
    PickupQueueItem item = newItemWithStatus(QueueStatus.EN_ROUTE, false);
    doThrow(new AmqpException("broker offline"))
        .when(rabbitTemplate)
        .convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_ARRIVAL_ANNOUNCED),
            any(ArrivalAnnouncedEvent.class));

    assertThatThrownBy(() -> adapter.notifyStudentArrivalAnnounced(item))
        .isInstanceOf(AmqpException.class)
        .hasMessageContaining("broker offline");
  }

  @Test
  @DisplayName(
      "notifyStatusChanged publishes to school.queue.events with queue.status.changed routing"
          + " key, including previous and new status")
  void shouldPublishStatusChangedToConfiguredExchangeAndRoutingKey() {
    PickupQueueItem item = newItemWithStatus(QueueStatus.ARRIVED, true);
    Instant before = Instant.now();

    adapter.notifyStatusChanged(item, QueueStatus.EN_ROUTE);

    ArgumentCaptor<StatusChangedEvent> captor = ArgumentCaptor.forClass(StatusChangedEvent.class);
    verify(rabbitTemplate)
        .convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED),
            captor.capture());
    Instant after = Instant.now();

    StatusChangedEvent event = captor.getValue();
    assertThat(event.queueItemId()).isEqualTo(item.id());
    assertThat(event.studentId()).isEqualTo(item.studentId());
    assertThat(event.schoolId()).isEqualTo(item.schoolId());
    assertThat(event.previousStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(event.newStatus()).isEqualTo(QueueStatus.ARRIVED);
    assertThat(event.called()).isTrue();
    assertThat(event.currentRange()).isEqualTo(ProximityRange.MEDIUM);
    assertThat(event.occurredAt()).isBetween(before, after);
  }

  @Test
  @DisplayName("notifyStatusChanged propagates AmqpException from the broker")
  void shouldPropagateAmqpExceptionWhenBrokerFailsOnStatusChange() {
    PickupQueueItem item = newItemWithStatus(QueueStatus.ARRIVED, true);
    doThrow(new AmqpException("broker offline"))
        .when(rabbitTemplate)
        .convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_NAME),
            eq(RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED),
            any(StatusChangedEvent.class));

    assertThatThrownBy(() -> adapter.notifyStatusChanged(item, QueueStatus.EN_ROUTE))
        .isInstanceOf(AmqpException.class)
        .hasMessageContaining("broker offline");
  }
}
