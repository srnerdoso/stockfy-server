package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {

  private final String itemNotFound;

  public EntityNotFoundException(String itemNotFound) {
    this.itemNotFound = itemNotFound;
  }
}
