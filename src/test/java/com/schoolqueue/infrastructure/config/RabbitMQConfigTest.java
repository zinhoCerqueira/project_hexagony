package com.schoolqueue.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RabbitMQConfigTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(RabbitMQConfig.class);

  @Test
  @DisplayName("declares the durable queue.notifications")
  void shouldDeclareDurableQueueNotifications() {
    contextRunner.run(
        ctx -> {
          Queue queue = ctx.getBean(Queue.class);
          assertThat(queue.getName()).isEqualTo("queue.notifications");
          assertThat(queue.isDurable()).isTrue();
        });
  }

  @Test
  @DisplayName("declares the durable topic exchange school.queue.events")
  void shouldDeclareDurableSchoolQueueEventsExchange() {
    contextRunner.run(
        ctx -> {
          TopicExchange exchange = ctx.getBean(TopicExchange.class);
          assertThat(exchange.getName()).isEqualTo("school.queue.events");
          assertThat(exchange.isDurable()).isTrue();
          assertThat(exchange.isAutoDelete()).isFalse();
        });
  }

  @Test
  @DisplayName("binds queue.notifications to school.queue.events with both routing keys")
  void shouldBindQueueToExchangeWithBothRoutingKeys() {
    contextRunner.run(
        ctx -> {
          Map<String, Binding> bindings = ctx.getBeansOfType(Binding.class);
          assertThat(bindings).hasSize(2);

          assertThat(bindings.values())
              .extracting(
                  Binding::getDestination, Binding::getDestinationType, Binding::getExchange)
              .containsOnly(
                  org.assertj.core.groups.Tuple.tuple(
                      "queue.notifications", Binding.DestinationType.QUEUE, "school.queue.events"));

          assertThat(bindings.values())
              .extracting(Binding::getRoutingKey)
              .containsExactlyInAnyOrder("queue.arrival.announced", "queue.status.changed");
        });
  }

  @Test
  @DisplayName("exposes the exchange/queue/routing key names as public constants")
  void shouldExposeMessagingNamesAsConstants() {
    assertThat(RabbitMQConfig.EXCHANGE_NAME).isEqualTo("school.queue.events");
    assertThat(RabbitMQConfig.QUEUE_NAME).isEqualTo("queue.notifications");
    assertThat(RabbitMQConfig.ROUTING_KEY_ARRIVAL_ANNOUNCED).isEqualTo("queue.arrival.announced");
    assertThat(RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED).isEqualTo("queue.status.changed");
  }
}
