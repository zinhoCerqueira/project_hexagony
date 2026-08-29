package com.schoolqueue.domain.exception;

public class ClassroomNotFoundException extends RuntimeException {

  public ClassroomNotFoundException(String message) {
    super(message);
  }
}
