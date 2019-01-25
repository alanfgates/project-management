package com.github.alanfgates.project.management;

public class StreamNotEmptyException extends Exception {

  public StreamNotEmptyException(String message) {
    super(message);
  }

  public StreamNotEmptyException(String message, Throwable cause) {
    super(message, cause);
  }
}
