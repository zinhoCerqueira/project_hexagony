package com.schoolqueue.domain.exception;

public class SchoolNotFoundException extends RuntimeException {

  public SchoolNotFoundException(String message) {
    super(message);
  }
}
