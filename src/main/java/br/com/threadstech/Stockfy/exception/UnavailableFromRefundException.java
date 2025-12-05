package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class UnavailableFromRefundException extends RuntimeException {

  private final Long refoundId;

  public UnavailableFromRefundException(Long refoundId) {
    this.refoundId = refoundId;
  }
}
