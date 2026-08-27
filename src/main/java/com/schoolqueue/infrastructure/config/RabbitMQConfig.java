package com.schoolqueue.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "school.queue.events";
  public static final String QUEUE_NAME = "queue.notifications";
  public static final String ROUTING_KEY_ARRIVAL_ANNOUNCED = "queue.arrival.announced";
  public static final String ROUTING_KEY_STATUS_CHANGED = "queue.status.changed";

  @Bean
  public Queue queueNotifications() {
    return new Queue(QUEUE_NAME, true);
  }

  @Bean
  public TopicExchange schoolQueueEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  @Bean
  public Binding queueNotificationsBindingArrival(
      Queue queueNotifications, TopicExchange schoolQueueEventsExchange) {
    return BindingBuilder.bind(queueNotifications)
        .to(schoolQueueEventsExchange)
        .with(ROUTING_KEY_ARRIVAL_ANNOUNCED);
  }

  @Bean
  public Binding queueNotificationsBindingStatusChanged(
      Queue queueNotifications, TopicExchange schoolQueueEventsExchange) {
    return BindingBuilder.bind(queueNotifications)
        .to(schoolQueueEventsExchange)
        .with(ROUTING_KEY_STATUS_CHANGED);
  }

  @Bean
  public Jackson2JsonMessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplateCustomizer rabbitTemplateJsonCustomizer(
      Jackson2JsonMessageConverter jsonMessageConverter) {
    return template -> template.setMessageConverter(jsonMessageConverter);
  }
}
