package com.example.ainote.common.exception;

public class MissingUserHeaderException extends IllegalArgumentException {
  public MissingUserHeaderException(String message) { 
    super(message); 
  }
}