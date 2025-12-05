package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {

  private final String itemNotFound;

  /**
   * Constructor.
   *
   * <p>Exemplo de erro: "Jon Doe não foi encontrado."
   *
   * @param itemNotFound O item que não foi encontrado. Preenche o parâmetro do erro, por exemplo
   *     "Jon Doe" do exemplo acima. "{0} não foi encontrado." Onde "{0}" é o item que não foi
   *     encontrado.
   */
  public EntityNotFoundException(String itemNotFound) {
    this.itemNotFound = itemNotFound;
  }
}
