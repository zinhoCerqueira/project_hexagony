package com.schoolqueue.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchoolNotFoundExceptionTest {

  @Test
  @DisplayName("extends RuntimeException")
  void shouldExtendRuntimeException() {
    assertThat(RuntimeException.class).isAssignableFrom(SchoolNotFoundException.class);
  }

  @Test
  @DisplayName("carries the given message")
  void shouldCarryTheGivenMessage() {
    SchoolNotFoundException exception = new SchoolNotFoundException("Escola não encontrada");

    assertThat(exception).hasMessage("Escola não encontrada");
  }
}
