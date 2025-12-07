package br.com.threadstech.stockfy.exception;

import br.com.threadstech.stockfy.enums.PasswordKey;
import lombok.Getter;

@Getter
public class InvalidPasswordException extends RuntimeException {

  private final String passwordKey;

  /**
   * Constrói uma exceção de senha inválida.
   *
   * <p>Seta uma chave de senha inválida para que a mensagem de erro seja identificada no arquivo
   * messages.properties.
   *
   * <p>Exemplo:
   *
   * <pre>{@code
   * public void updatePasswordById(Long id, @Valid PasswordUpdateDto passwordUpdateDto) {
   *   String currentPassword = findPasswordById(id)
   *
   *   if (!passwordUpdateDto.getCurrentPassword().equals(currentPassword)) {
   *     // Seta o 'code' da mensagem de erro para "exception.invalidPasswordException.current"
   *     throw new InvalidPasswordException(PasswordKey.CURRENT);
   *   }
   *   if (!passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmPassword())) {
   *     // Seta o 'code' da mensagem de erro para "exception.invalidPasswordException.confirm"
   *     throw new InvalidPasswordException(PasswordKey.CONFIRM);
   *   }
   * }
   * }</pre>
   *
   * @param passwordKey Chave da senha inválida.
   */
  public InvalidPasswordException(PasswordKey passwordKey) {
    this.passwordKey = passwordKey.name().toLowerCase();
  }
}
