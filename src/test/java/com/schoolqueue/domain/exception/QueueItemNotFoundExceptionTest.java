package com.schoolqueue.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueueItemNotFoundExceptionTest {

  @Test
  @DisplayName("extends RuntimeException")
  void shouldExtendRuntimeException() {
    assertThat(RuntimeException.class).isAssignableFrom(QueueItemNotFoundException.class);
  }

  @Test
  @DisplayName("carries the given message")
  void shouldCarryTheGivenMessage() {
    QueueItemNotFoundException exception =
        new QueueItemNotFoundException("Item da fila não encontrado");

    assertThat(exception).hasMessage("Item da fila não encontrado");
  }
}
