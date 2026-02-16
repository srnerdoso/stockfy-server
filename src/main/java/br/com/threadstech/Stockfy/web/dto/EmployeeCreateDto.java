package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.validation.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.br.CPF;

/*
 * FIXME: Fluxo de criação de senha no cadastro de funcionários
 *
 * Atualmente este DTO permite que a senha do funcionário seja definida
 * diretamente durante o registro feito por um operador do sistema.
 * Esse modelo cria riscos de segurança, privacidade e responsabilidade,
 * pois terceiros passam a ter conhecimento ou controle sobre credenciais
 * que deveriam ser exclusivas do usuário final.
 *
 * Problemas do modelo atual:
 * - exposição indevida de credenciais durante o cadastro
 * - possibilidade de reutilização ou compartilhamento de senha
 * - aumento da superfície de abuso interno
 * - dificuldade de auditoria sobre quem teve acesso à senha
 *
 * Fluxo recomendado:
 * - o cadastro inicial NÃO deve incluir senha
 * - o sistema deve gerar um token de ativação com expiração
 * - o funcionário deve criar a própria senha no primeiro acesso
 * - a conta só deve ser ativada após a definição da senha
 *
 * Este DTO deve ser revisado futuramente para:
 * - remover o campo de senha do registro administrativo
 * - separar o fluxo de onboarding/auto-definição de senha
 * - alinhar o processo com boas práticas de segurança de credenciais
 *
 * Importante: qualquer senha definida deve ser processada exclusivamente
 * por mecanismos seguros (hash forte, validação e políticas internas),
 * nunca sendo armazenada ou trafegada em formato reversível.
 */

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDto {

  @NotBlank(message = "{NotBlank.employeeDto.fullName}")
  @Size(min = 2, max = 255, message = "{Size.employeeDto.fullName}")
  private String fullName;

  @CPF(message = "{CPF.employeeDto.cpf}")
  @NotBlank(message = "{NotBlank.employeeDto.cpf}")
  private String cpf;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "{NotNull.employeeDto.birthday}")
  private LocalDate birthday;

  @NotNull(message = "{NotNull.employeeDto.contact}")
  private ContactCreateDto contact;

  @NotNull(message = "{NotNull.employeeDto.address}")
  private AddressCreateDto address;

  @Nullable
  @Role(message = "{Role.employeeDto.role}")
  private String role;

  @NotBlank(message = "{NotBlank.password}")
  @Size(min = 8, max = 64, message = "{Size.password}")
  private String password;
}
