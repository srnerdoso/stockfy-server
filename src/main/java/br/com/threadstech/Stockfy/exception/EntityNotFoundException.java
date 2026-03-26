package br.com.threadstech.stockfy.exception;

import lombok.Getter;

// FIXME: Esta classe constrói um erro genérico. Ela deve ser capaz de informar qual entidade está
//        sendo referenciada invés de mapear um erro simples como por exemplo "123456 não foi
//        encontrado". Ela deve receber um parâmetro com caminho principal do message.properties.
//        Por exemplo "product", fazendo com que este parâmetro consiga identificar qual erro
//        retornar com base no message.properties. Exemplo: imaginemos que exista uma propriedade no
//        message.properties com caminho "notFound.product.barcode". O construtor da exception
//        recebe o parâmetro "product.barcode" e o item que não foi encontrado, por exemplo
//        "1234567890". O Advice conseguirá identificar o erro e retornar o message.properties
//        correspondente unindo o caminho pai "notFound." com "product.barcode" e inferindo o item
//        que não foi encontrado resultando em uma mensagem por exemplo "O produto com código de
//        barras 1234567890 não foi encontrado.".

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
