package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.exception.InvalidQueueStateException;
import com.schoolqueue.domain.exception.SchoolNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  public record FieldError(String field, String message) {}

  public record ValidationErrorResponse(int status, List<FieldError> errors) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    List<FieldError> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), errors));
  }

  @ExceptionHandler(InvalidQueueStateException.class)
  public ResponseEntity<ValidationErrorResponse> handleInvalidQueueState(
      InvalidQueueStateException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ValidationErrorResponse(
                HttpStatus.CONFLICT.value(),
                List.of(new FieldError("state", exception.getMessage()))));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ValidationErrorResponse> handleIllegalState(
      IllegalStateException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                List.of(new FieldError("state", exception.getMessage()))));
  }

  @ExceptionHandler(SchoolNotFoundException.class)
  public ResponseEntity<ValidationErrorResponse> handleSchoolNotFound(
      SchoolNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ValidationErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                List.of(new FieldError("schoolId", exception.getMessage()))));
  }

  @ExceptionHandler(ClassroomNotFoundException.class)
  public ResponseEntity<ValidationErrorResponse> handleClassroomNotFound(
      ClassroomNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ValidationErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                List.of(new FieldError("classroomId", exception.getMessage()))));
  }
}
