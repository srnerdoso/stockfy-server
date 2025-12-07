package br.com.threadstech.stockfy.config.constraints;

public interface ConstraintNames<T> {

  /**
   * Realiza uma busca em um {@code Map<String, String>}.
   *
   * <ul>
   *   <h5>O Map contém:</h5>
   *   <li>Chave: nome técnico da constraint no banco de dados (ex.: uk_customer_cpf).
   *   <li>Valor: parte final da chave usada em "/src/main/resources/messages.properties" para i18n
   *       (ex.: "cpf", que será usada como "constraint.customer.cpf").
   * </ul>
   *
   * <p>Exemplo de uso:
   *
   * <pre>{@code
   * // retorna "cpf", que pode ser usada como "constraint.customer.cpf" no MessageSource
   * String key = CustomerConstraintNames.getI18nKeyMap(CustomerConstraintNames.UK_CPF);
   * }</pre>
   *
   * @param rawConstraint Nome usado para identificar a constraint no banco de dados. Por exemplo:
   *     "uk_customer_cpf".
   * @return O nome legível do constraint. Por exemplo: {@code
   *     CustomerConstraintNames.getI18nKeyMap("uk_customer_cpf")} // Retorna "CPF".
   */
  String getI18nKeyMap(String rawConstraint);

  /**
   * Retorna o nome da entidade usada como parte da chave de i18n para buscar mensagens na interface
   * do {@link org.springframework.context.MessageSource MessageSource}.
   *
   * <p>Exemplo: para a entidade {@code Customer}, este método retornará {@code "customer"}. Essa
   * string pode ser usada para construir a chave completa, por exemplo: {@code
   * "constraint.customer.cpf"}.
   *
   * @return o nome da entidade em lowercase, utilizada como parte da chave de i18n
   */
  Class<T> getI18nEntityClass();
}
