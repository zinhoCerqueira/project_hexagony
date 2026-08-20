package com.schoolqueue.domain.exception;

public class StudentNotFoundException extends RuntimeException {

  public StudentNotFoundException(String message) {
    super(message);
  }
}
