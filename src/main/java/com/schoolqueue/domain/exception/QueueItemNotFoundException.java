package com.schoolqueue.domain.exception;

public class QueueItemNotFoundException extends RuntimeException {

  public QueueItemNotFoundException(String message) {
    super(message);
  }
}
