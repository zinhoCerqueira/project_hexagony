package com.schoolqueue.infrastructure.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("InvalidQueueStateException maps to 409 Conflict with field=state and original message")
  void shouldMapInvalidQueueStateExceptionTo409() {
    InvalidQueueStateException exception = new InvalidQueueStateException("Fila já finalizada ou cancelada");

    ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
        handler.handleInvalidQueueState(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(409);
    assertThat(response.getBody().errors())
        .hasSize(1)
        .first()
        .extracting(GlobalExceptionHandler.FieldError::field, GlobalExceptionHandler.FieldError::message)
        .containsExactly("state", "Fila já finalizada ou cancelada");
  }

  @Test
  @DisplayName("IllegalStateException maps to 400 Bad Request with field=state and original message")
  void shouldMapIllegalStateExceptionTo400() {
    IllegalStateException exception =
        new IllegalStateException("Já existe um aviso de saída ativo para este aluno.");

    ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
        handler.handleIllegalState(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().errors())
        .hasSize(1)
        .first()
        .extracting(GlobalExceptionHandler.FieldError::field, GlobalExceptionHandler.FieldError::message)
        .containsExactly("state", "Já existe um aviso de saída ativo para este aluno.");
  }

  @Test
  @DisplayName("MethodArgumentNotValidException maps to 400 with one FieldError per invalid field")
  void shouldMapMethodArgumentNotValidTo400WithFieldErrors() throws NoSuchMethodException {
    MethodParameter parameter = methodParameterOf("announce", AnnounceArrivalRequestMarker.class);
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "announceRequest");
    bindingResult.addError(new FieldError("announceRequest", "latitude", "must not be null"));
    bindingResult.addError(new FieldError("announceRequest", "longitude", "must not be null"));
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
        handler.handleMethodArgumentNotValid(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    List<GlobalExceptionHandler.FieldError> errors = response.getBody().errors();
    assertThat(errors).extracting(GlobalExceptionHandler.FieldError::field)
        .containsExactlyInAnyOrder("latitude", "longitude");
    assertThat(errors).extracting(GlobalExceptionHandler.FieldError::message)
        .containsExactlyInAnyOrder("must not be null", "must not be null");
  }

  private static MethodParameter methodParameterOf(String methodName, Class<?> declaringClass)
      throws NoSuchMethodException {
    Method method = declaringClass.getDeclaredMethod(methodName, Object.class);
    return new MethodParameter(method, 0);
  }

  private static final class AnnounceArrivalRequestMarker {
    @SuppressWarnings("unused")
    public void announce(Object request) {}
  }
}
