package com.schoolqueue.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudentNotFoundExceptionTest {

  @Test
  @DisplayName("extends RuntimeException")
  void shouldExtendRuntimeException() {
    assertThat(RuntimeException.class).isAssignableFrom(StudentNotFoundException.class);
  }

  @Test
  @DisplayName("carries the given message")
  void shouldCarryTheGivenMessage() {
    StudentNotFoundException exception = new StudentNotFoundException("Aluno não encontrado");

    assertThat(exception).hasMessage("Aluno não encontrado");
  }
}
