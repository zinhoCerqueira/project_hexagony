package com.schoolqueue.infrastructure.adapters.out.messaging;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.infrastructure.adapters.out.messaging.dto.ArrivalAnnouncedEvent;
import com.schoolqueue.infrastructure.config.RabbitMQConfig;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQNotificationAdapter implements QueueNotificationPort {

  private static final Logger LOG = LoggerFactory.getLogger(RabbitMQNotificationAdapter.class);

  private final RabbitTemplate rabbitTemplate;

  public RabbitMQNotificationAdapter(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void notifyStudentArrivalAnnounced(PickupQueueItem item) {
    ArrivalAnnouncedEvent event =
        new ArrivalAnnouncedEvent(
            item.id(),
            item.studentId(),
            item.schoolId(),
            item.journeyStatus(),
            item.called(),
            item.currentRange(),
            Instant.now());

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ARRIVAL_ANNOUNCED, event);

    LOG.info("Published arrival.announced for queueItemId={}", item.id());
  }

  @Override
  public void notifyStatusChanged(PickupQueueItem item, QueueStatus previousStatus) {
    throw new UnsupportedOperationException(
        "notifyStatusChanged is not implemented yet in RabbitMQNotificationAdapter (see LAC08)");
  }
}
