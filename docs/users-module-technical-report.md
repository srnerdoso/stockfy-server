# Relatorio tecnico do modulo users

Data da analise: 2026-05-16

Escopo analisado: `C:\Projetos\stockfy\stockfy-server`

Observacoes de escopo:

- Codigo de producao do modulo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users`.
- Os testes ainda usam pacotes/caminhos `modules/users`, mas importam o codigo real em `br.com.threadstech.stockfy.users`.
- Os `AGENTS.md` citados pelo AGENTS raiz em `src/main/java/.../modules` nao existem neste checkout. Os arquivos locais existentes e usados foram:
  - `C:\Projetos\stockfy\stockfy-server\AGENTS.md`
  - `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\AGENTS.md`
  - `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\AGENTS.md`
  - `C:\Projetos\stockfy\stockfy-server\src\main\resources\AGENTS.md`
  - `C:\Projetos\stockfy\stockfy-server\src\test\AGENTS.md`

## 1. Cadeia comum de processamento HTTP

- [x] `RateLimitFilter.doFilterInternal()`
  - Arquivo: `RateLimitFilter.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\RateLimitFilter.java`
  - Papel: roda antes da autenticacao, consome bucket por metodo, rota e IP remoto.
  - Metodo: `doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`
  - Arquivos relacionados: `UserRateLimitConfig.java`, `UserRateLimitProperties.java`, `RateLimitDecision.java`, `RateLimitTimeConfig.java`, `application-dev.properties`, `application-prod.properties`, `application.properties`.
  - Trecho responsavel: `rateLimitConfig.consume(request.getMethod(), request.getRequestURI(), request.getRemoteAddr())`; se rejeitado, retorna `429` e escreve `"Too many requests"` quando a policy permitir.
  - Ponto critico: usa `request.getRemoteAddr()`, sem tratamento de `X-Forwarded-For`; atras de proxy pode limitar todos os usuarios pelo IP do proxy.

- [x] `JwtAuthenticationFilter.doFilterInternal()`
  - Arquivo: `JwtAuthenticationFilter.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\JwtAuthenticationFilter.java`
  - Papel: cria `Authentication` com principal `UUID` e authorities `ROLE_*` quando `access_token` e `refresh_token` existem e apontam para o mesmo usuario.
  - Metodo: `doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`, `toAuthority(String)`
  - Arquivos relacionados: `JwtService.java`, `TokenService.java`, `SecurityConfig.java`, `UnauthorizedAuthenticationEntryPoint.java`, `ForbiddenAccessDeniedResponder.java`.
  - Trecho responsavel: extrai `access_token` e `refresh_token`; valida `tokenService.validateRefreshToken(refreshToken)`; compara `authenticatedUserId.equals(refreshTokenUserId)`; seta `UsernamePasswordAuthenticationToken`.
  - Ponto critico: exige os dois cookies para autenticar; se o refresh token for removido do Redis, mesmo access token valido nao autentica.

- [x] `SecurityConfig.securityFilterChain()`
  - Arquivo: `SecurityConfig.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\config\SecurityConfig.java`
  - Papel: configura filtros, stateless session, entrada 401 sem corpo, 403 sem corpo e autorizacao HTTP base.
  - Metodo: `securityFilterChain(HttpSecurity)`, `passwordEncoder()`
  - Trecho responsavel:
    - `csrf(AbstractHttpConfigurer::disable)`
    - `addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)`
    - `addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class)`
    - `POST /api/v1/auth/sessions/**` e `PATCH /api/v1/users/password` como `permitAll`
    - `/api/v1/**` como `authenticated`
  - Ponto critico: CSRF esta desabilitado enquanto autenticacao usa cookies HttpOnly. Nao ha `SameSite` definido em `AuthController`.

- [x] Tratamento comum de erro de validacao
  - Arquivos:
    - `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\shared\exception\GlobalExceptionHandler.java`
    - `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\shared\exception\ApiErrorResponse.java`
    - `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\shared\exception\HttpMessageNotReadableErrorFactory.java`
    - `C:\Projetos\stockfy\stockfy-server\src\main\resources\messages.properties`
  - Metodos: `handleValidationExceptions`, `handleMessageNotReadable`, `handleMissingRequestParameter`, `handleArgumentTypeMismatch`, `handleConstraintViolation`, `ApiErrorResponse.badRequest(...)`.
  - Papel: gera `400` com `type`, `title`, `status`, `detail`, `instance`, `fieldErrors`.
  - Ponto critico: nao ha handler customizado para erro interno generico; `500` fica no mecanismo padrao do Spring Boot, com `server.error.include-stacktrace=never` apenas em prod e em testes especificos.

- [x] Tratamento de erros do modulo users
  - Arquivo: `UserExceptionHandler.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\UserExceptionHandler.java`
  - Metodos: `handlePasswordMismatchException`, `handleEmailAlreadyExistsException`, `handleInvalidPasswordException`, `handleInvalidPasswordResetCodeException`, `handleCurrentPasswordInvalidException`, `handleUserNotFoundException`, `handleInvalidCredentialsException`, `handleInvalidRefreshTokenException`, `handleInvalidUserRolesException`.
  - Papel: converte excecoes de aplicacao/dominio em `409`, `404`, `422`, `400` ou `401`.
  - Trecho responsavel: `InvalidCredentialsException` e `InvalidRefreshTokenException` retornam `ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()`, sem corpo.

## 2. Endpoints HTTP

### 2.1 POST /api/v1/auth/sessions

- [x] Contrato
  - Metodo HTTP: `POST`
  - Rota: `/api/v1/auth/sessions`
  - Controller: `AuthController.login()`
  - Arquivo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\controller\AuthController.java`
  - Request DTO: `LoginRequest`
  - Request DTO path: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\LoginRequest.java`
  - Response DTO externo: nenhum corpo (`ResponseEntity<Void>`)
  - Response DTO interno: `AuthResponse`, usado para popular cookies `access_token` e `refresh_token`
  - Response DTO path: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\AuthResponse.java`
  - Codigos HTTP possiveis pelo codigo/testes: `200`, `400`, `401`, `429`, `500`
  - Autenticacao/autorizacao: `permitAll` em `SecurityConfig.securityFilterChain()`
  - Rate limit: policy `login`, rota configurada em `application-dev.properties`, `application-prod.properties` e `src/test/resources/application.properties`

- [x] Validacoes
  - `LoginRequest.email`: `@NotBlank`, `@Email`.
  - `LoginRequest.password`: `@NotBlank`.
  - `LoginUseCase.parseEmail()`: cria `new Email(email)` e converte `IllegalArgumentException` em `InvalidCredentialsException`.
  - `GlobalExceptionHandler`: `400` para DTO invalido.
  - `UserExceptionHandler.handleInvalidCredentialsException()`: `401` sem corpo.

- [x] Regras de negocio executadas
  - `LoginUseCase.execute(email, password)`
    - busca usuario por email
    - rejeita usuario inativo ou `LOCKED`
    - compara senha com `PasswordEncoder.matches`
    - conta tentativa invalida no Redis (`login_attempts:<email>`)
    - bloqueia usuario apos 15 falhas
    - publica `AccountLockedEvent`
    - remove contador de falhas em login bem-sucedido
    - gera JWT de acesso
    - gera refresh token no Redis
  - `AuthController.addCookies()`
    - cria cookies `HttpOnly`, `Secure`, path `/`, max-age configurado.

- [x] Fluxo completo

```text
AuthController.login()
|-- LoginUseCase.execute()
|   |-- LoginUseCase.parseEmail()
|   |-- UserRepository.findByEmail()
|   |   `-- JpaUserRepositoryAdapter.findByEmail()
|   |       |-- SpringDataUserRepository.findByEmail()
|   |       `-- UserPersistenceMapper.toDomain()
|   |-- PasswordEncoder.matches()
|   |-- LoginUseCase.handleFailedLogin()
|   |   |-- StringRedisTemplate.opsForValue().increment()
|   |   |-- StringRedisTemplate.expire()
|   |   |-- User.lock()
|   |   |-- UserRepository.update()
|   |   `-- RabbitMqEventPublisher.publish(AccountLockedEvent)
|   |-- LoginUseCase.resetFailedAttempts()
|   |-- JwtService.generateToken()
|   `-- TokenService.generateRefreshToken()
`-- AuthController.addCookies()
```

- [x] Arquivos participantes diretos e indiretos
  - `AuthController.java`: controller, `login`, `addCookies`; risco: cookies sem `SameSite`.
  - `LoginRequest.java`: validacao de entrada; risco: formato de email tambem e validado depois pelo VO.
  - `AuthResponse.java`: transporte interno de tokens; risco: nao deve ser retornado no body, e hoje nao e retornado.
  - `LoginUseCase.java`: regra de autenticacao, lock e evento; risco: publish RabbitMQ dentro do fluxo transacional.
  - `Email.java`: validacao de email no dominio; risco: regex limita TLD a 2-6 caracteres.
  - `User.java`: `lock`; risco: multiplas tentativas concorrentes podem publicar mais de um evento de lock.
  - `UserRepository.java`, `JpaUserRepositoryAdapter.java`, `SpringDataUserRepository.java`, `UserPersistenceMapper.java`, `UserJpaEntity.java`: busca/update do usuario.
  - `JwtService.java`: gera JWT assinado com roles.
  - `TokenService.java`: salva refresh token em Redis.
  - `HmacSha256Hasher.java`: gera HMAC para chave Redis do refresh token.
  - `RabbitMqEventPublisher.java`, `RabbitMqConfig.java`, `AccountLockedEvent.java`: evento de bloqueio.
  - `SecurityConfig.java`, `RateLimitFilter.java`, `UserRateLimitConfig.java`, `UserRateLimitProperties.java`, `RateLimitDecision.java`, `RateLimitTimeConfig.java`: cadeia de seguranca/rate limit.
  - `UserExceptionHandler.java`, `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `messages.properties`: respostas de erro.
  - Testes relacionados: `LoginUserIT.java`, `LoginUserInternalErrorIT.java`, `AuthControllerTests.java`, `AuthenticationTests.java`, `TokenServiceTests.java`, `HmacSha256HasherTests.java`, `RateLimitFilterTests.java`, `UserRateLimitConfigTests.java`, `AccountLockedEventTests.java`.

### 2.2 POST /api/v1/auth/sessions/refresh

- [x] Contrato
  - Metodo HTTP: `POST`
  - Rota: `/api/v1/auth/sessions/refresh`
  - Controller: `AuthController.refresh()`
  - Request DTO: nenhum; entrada vem do cookie `refresh_token`, `required = false`.
  - Response DTO externo: nenhum corpo (`ResponseEntity<Void>`)
  - Response DTO interno: `AuthResponse`, usado para novos cookies.
  - Codigos HTTP possiveis: `200`, `401`, `429`, `500`
  - Autenticacao/autorizacao: `permitAll` em `SecurityConfig.securityFilterChain()`
  - Rate limit: policy `refresh`, com bucket normal e `blockCapacity`.

- [x] Validacoes e regras
  - `RefreshTokenUseCase.execute()` rejeita `null`, blank ou token ausente no Redis.
  - `TokenService.consumeRefreshToken()` usa `Redis GETDEL` via `getAndDelete`, retornando `Optional<UUID>`.
  - Usuario precisa existir, estar `active = true` e status diferente de `LOCKED`.
  - Token antigo e consumido antes de emitir novo refresh token.

- [x] Fluxo completo

```text
AuthController.refresh()
|-- RefreshTokenUseCase.execute()
|   |-- TokenService.validateRefreshToken()
|   |-- RefreshTokenUseCase.consumeRefreshToken()
|   |   `-- TokenService.consumeRefreshToken()
|   |       `-- StringRedisTemplate.opsForValue().getAndDelete()
|   |-- UserRepository.findById()
|   |-- JwtService.generateToken()
|   `-- TokenService.generateRefreshToken()
`-- AuthController.addCookies()
```

- [x] Arquivos participantes
  - `AuthController.java`: `refresh`, `addCookies`.
  - `RefreshTokenUseCase.java`: orquestra rotacao; ponto critico: anotado como `@Transactional(readOnly = true)` apesar de consumir/gerar estado em Redis.
  - `TokenService.java`: `validateRefreshToken`, `consumeRefreshToken`, `generateRefreshToken`.
  - `JwtService.java`: novo access token.
  - `UserRepository.java`, `JpaUserRepositoryAdapter.java`, `SpringDataUserRepository.java`, `UserPersistenceMapper.java`, `UserJpaEntity.java`: busca do usuario.
  - `HmacSha256Hasher.java`: hash do refresh token para chave Redis.
  - `UserExceptionHandler.java`: `InvalidRefreshTokenException` vira `401` sem corpo.
  - `SecurityConfig.java`, `RateLimitFilter.java`, `UserRateLimitConfig.java`: acesso publico + limitacao.
  - Testes relacionados: `RefreshTokenIT.java`, `RefreshTokenInternalErrorIT.java`, `AuthenticationTests.java`, `TokenServiceTests.java`, `JwtAuthenticationFilterTests.java`.

### 2.3 DELETE /api/v1/auth/sessions/current

- [x] Contrato
  - Metodo HTTP: `DELETE`
  - Rota: `/api/v1/auth/sessions/current`
  - Controller: `AuthController.logout()`
  - Request DTO: nenhum; entrada vem do cookie `refresh_token`.
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `204`, `401`, `429`, `500`
  - Autenticacao/autorizacao: autenticado por `/api/v1/**` em `SecurityConfig`; nao ha `@PreAuthorize` no metodo.
  - Rate limit: policy `logout`, `write-body=false`.

- [x] Regras
  - `JwtAuthenticationFilter` autentica somente se access e refresh cookies forem validos e pertencem ao mesmo usuario.
  - `LogoutUseCase.execute()` revoga o refresh token atual no Redis.
  - `AuthController.clearCookies()` expira `access_token` e `refresh_token`.

- [x] Fluxo completo

```text
RateLimitFilter.doFilterInternal()
`-- JwtAuthenticationFilter.doFilterInternal()
    `-- AuthController.logout()
        |-- LogoutUseCase.execute()
        |   `-- TokenService.revokeRefreshToken()
        `-- AuthController.clearCookies()
```

- [x] Arquivos participantes
  - `AuthController.java`: `logout`, `clearCookies`, `expiredCookie`.
  - `LogoutUseCase.java`: revoga refresh token.
  - `TokenService.java`: remove chave Redis.
  - `JwtAuthenticationFilter.java`: protege endpoint antes do controller.
  - `UnauthorizedAuthenticationEntryPoint.java`: 401 sem corpo.
  - `SecurityConfig.java`, `RateLimitFilter.java`, `UserRateLimitConfig.java`.
  - Testes relacionados: `LogoutUserIT.java`, `LogoutUserInternalErrorIT.java`, `AuthenticationTests.java`, `TokenServiceTests.java`.

### 2.4 POST /api/v1/users

- [x] Contrato
  - Metodo HTTP: `POST`
  - Rota: `/api/v1/users`
  - Controller: `UserController.register()`
  - Request DTO: `RegisterUserRequest`
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `201`, `400`, `401`, `403`, `409`, `422`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Validacoes
  - `name`: `@NotBlank`, `@Pattern("^[\\p{L}\\p{M}0-9 .'-]+$")`
  - `email`: `@NotBlank`, `@Email`
  - `password`: `@NotBlank`
  - `role`: `@NotNull`
  - `confirmPassword`: validacao manual em `RegisterUserUseCase.execute()`.
  - `Password` VO exige minimo 8 caracteres.

- [x] Regras
  - Cria `User` com `UUID.randomUUID()`, email VO, senha BCrypt, role unica vinda da request.
  - Persiste por `UserRepository.save()`.
  - Duplicidade de email e traduzida por `JpaUserRepositoryAdapter.translateConstraintViolation()`.

- [x] Fluxo completo

```text
UserController.register()
`-- RegisterUserUseCase.execute()
    |-- PasswordEncoder.encode()
    |-- new Email()
    |-- new Password()
    `-- UserRepository.save()
        `-- JpaUserRepositoryAdapter.save()
            |-- UserPersistenceMapper.toEntity()
            `-- SpringDataUserRepository.saveAndFlush()
```

- [x] Arquivos participantes
  - `UserController.java`: `register`.
  - `RegisterUserRequest.java`: DTO e validacoes.
  - `RegisterUserUseCase.java`: criacao do usuario.
  - `User.java`, `Email.java`, `Password.java`, `UserRole.java`, `UserStatus.java`: dominio.
  - `UserRepository.java`, `JpaUserRepositoryAdapter.java`, `SpringDataUserRepository.java`, `UserPersistenceMapper.java`, `UserJpaEntity.java`.
  - `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `RateLimitFilter.java`.
  - `UserExceptionHandler.java`, `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `messages.properties`.
  - Testes relacionados: `UserCreationIT.java`, `UserManagementTests.java`, `UserRepositoryIT.java`, `JpaUserRepositoryAdapterTests.java`, `PasswordTests.java`, `EmailTests.java`.

### 2.5 PATCH /api/v1/users/{id}/roles

- [x] Contrato
  - Metodo HTTP: `PATCH`
  - Rota: `/api/v1/users/{id}/roles`
  - Controller: `UserController.updateUserRoles()`
  - Request DTO: `UpdateUserRolesRequest`
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `204`, `400`, `401`, `403`, `404`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Validacoes
  - `id`: conversao Spring para `UUID`; erro vira `400`.
  - `add` e `remove`: lista de strings com `@NotNull` e `@ValidUserRole`.
  - `ValidUserRoleValidator.isValid()`: aceita null para delegar ao `@NotNull`, rejeita blank e valores fora de `UserRole`.

- [x] Regras
  - Busca usuario.
  - Converte strings para `UserRole`.
  - `User.updateRoles()` adiciona/remove e impede usuario sem nenhuma role.
  - Persiste alteracao.

- [x] Fluxo completo

```text
UserController.updateUserRoles()
|-- UpdateUserRolesRequest.rolesToAdd()
|-- UpdateUserRolesRequest.rolesToRemove()
`-- UpdateUserRolesUseCase.execute()
    |-- UserRepository.findById()
    |-- User.updateRoles()
    `-- UserRepository.update()
```

- [x] Arquivos participantes
  - `UserController.java`: `updateUserRoles`.
  - `UpdateUserRolesRequest.java`: DTO, copia defensiva e conversao de roles.
  - `ValidUserRole.java`, `ValidUserRoleValidator.java`: validacao customizada.
  - `UpdateUserRolesUseCase.java`: orquestracao.
  - `User.java`, `UserRole.java`, `InvalidUserRolesException.java`.
  - Repositorios/mappers/persistencia comuns.
  - Handlers, security e rate limit comuns.
  - Testes relacionados: `UpdateUserRolesIT.java`, `UpdateUserRolesInternalErrorIT.java`, `UpdateUserRolesUseCaseTests.java`, `ValidUserRoleValidatorTests.java`, `UserTests.java`.

### 2.6 POST /api/v1/users/{id}/password-reset-codes

- [x] Contrato
  - Metodo HTTP: `POST`
  - Rota: `/api/v1/users/{id}/password-reset-codes`
  - Controller: `UserController.generateResetCode()`
  - Request DTO: nenhum; `id` no path.
  - Response DTO: `UserController.ResetCodeResponse`
  - Codigos HTTP possiveis: `200`, `400`, `401`, `403`, `404`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Regras
  - Busca usuario por ID.
  - Gera codigo numerico de 6 digitos com `SecureRandom`.
  - Armazena apenas HMAC SHA-256 do codigo.
  - Define expiracao por `PasswordResetProperties.codeExpiration()`.
  - Retorna o codigo em resposta.

- [x] Fluxo completo

```text
UserController.generateResetCode()
`-- GenerateResetCodeUseCase.execute()
    |-- UserRepository.findById()
    |-- SecureRandom.nextInt(1000000)
    |-- HmacSha256Hasher.hash()
    |-- User.setResetPasswordCodeHash()
    |-- User.setResetPasswordExpiresAt()
    `-- UserRepository.update()
```

- [x] Arquivos participantes
  - `UserController.java`: `generateResetCode`, `ResetCodeResponse`.
  - `GenerateResetCodeUseCase.java`: geracao/hash/expiracao.
  - `PasswordResetProperties.java`: duracao configuravel.
  - `HmacSha256Hasher.java`, `SecurityProperties.java`: HMAC.
  - Repositorios/mappers/persistencia comuns.
  - Testes relacionados: `GenerateResetCodeIT.java`, `GenerateResetCodeInternalErrorIT.java`, `PasswordResetTests.java`, `HmacSha256HasherTests.java`.
  - Ponto critico: o codigo e retornado diretamente no HTTP; nao existe integracao de email/SMS no modulo.

### 2.7 PATCH /api/v1/users/password

- [x] Contrato
  - Metodo HTTP: `PATCH`
  - Rota: `/api/v1/users/password`
  - Controller: `UserController.updatePassword()`
  - Request DTO: `UpdatePasswordRequest`
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `204`, `400`, `401`, `404`, `422`, `429`, `500`
  - Autenticacao/autorizacao: HTTP `permitAll`, metodo com `@PreAuthorize("#request.code() != null or !isAnonymous()")`
  - Rate limit: policy `password`.

- [x] Validacoes
  - `code`: `@Pattern("^\\d{6}$")` quando informado.
  - `newPassword`: `@NotBlank`, `@Size(min = 8)`.
  - `confirmPassword`: `@NotBlank`, `@Size(min = 8)`.
  - `currentPassword`: validacao no use case quando fluxo autenticado.

- [x] Regras
  - Com `code`: valida confirmacao, busca usuario por hash do codigo, valida expiracao, troca senha, remove codigo e expiracao.
  - Sem `code`: exige autenticacao, valida senha atual, conta tentativa invalida em Redis, bloqueia usuario apos 15 erros em 5 minutos, troca senha.
  - Nao revoga refresh tokens ja emitidos apos troca de senha.

- [x] Fluxo completo

```text
UserController.updatePassword()
|-- if request.code() != null
|   `-- UpdatePasswordUseCase.executeWithCode()
|       |-- validatePasswordConfirmation()
|       |-- HmacSha256Hasher.hash()
|       |-- UserRepository.findByResetPasswordCodeHash()
|       |-- resetCodeNotExpired()
|       |-- PasswordEncoder.encode()
|       `-- UserRepository.update()
`-- else authenticated
    `-- UpdatePasswordUseCase.executeAuthenticated()
        |-- UserRepository.findById()
        |-- PasswordEncoder.matches()
        |-- countInvalidAttempt()
        |   |-- StringRedisTemplate.opsForValue().increment()
        |   |-- StringRedisTemplate.expire()
        |   |-- User.lock()
        |   `-- UserRepository.update()
        |-- StringRedisTemplate.delete()
        |-- PasswordEncoder.encode()
        `-- UserRepository.update()
```

- [x] Arquivos participantes
  - `UserController.java`: `updatePassword`.
  - `UpdatePasswordRequest.java`: DTO.
  - `UpdatePasswordUseCase.java`: regras de reset e troca autenticada.
  - `HmacSha256Hasher.java`, `Password.java`, `User.java`, `UserRepository.java`.
  - `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `RateLimitFilter.java`.
  - `UserExceptionHandler.java`.
  - Testes relacionados: `UpdatePasswordIT.java`, `UpdatePasswordInternalErrorIT.java`, `UpdatePasswordUseCaseTests.java`, `PasswordResetTests.java`.

### 2.8 GET /api/v1/users/me

- [x] Contrato
  - Metodo HTTP: `GET`
  - Rota: `/api/v1/users/me`
  - Controller: `UserController.me()`
  - Request DTO: nenhum.
  - Response DTO: `UserResponse`
  - Codigos HTTP possiveis: `200`, `401`, `404`, `429`, `500`
  - Autenticacao/autorizacao: autenticado por `/api/v1/**`; ownership implicito pelo principal autenticado.
  - Rate limit: policy default `general`.

- [x] Regras
  - Extrai `UUID` de `Authentication.getPrincipal()`.
  - Busca diretamente no `UserRepository` dentro do controller.
  - `UserResponseMapperFactory.toResponse()` escolhe resposta completa para ADMIN, owner para dono, publica para terceiro.

- [x] Fluxo completo

```text
UserController.me()
|-- Authentication.getPrincipal()
|-- UserRepository.findById()
`-- UserResponseMapperFactory.toResponse()
    |-- SecurityContextHolder.getContext().getAuthentication()
    |-- UserResponseMapper.toFullResponse()
    |-- UserResponseMapper.toOwnerResponse()
    `-- UserResponseMapper.toPublicResponse()
```

- [x] Arquivos participantes
  - `UserController.java`: `me`.
  - `UserResponse.java`, `UserResponseMapperFactory.java`, `UserResponseMapper.java`.
  - Repositorios/mappers/persistencia comuns.
  - Testes relacionados: `UserControllerTests.java`, `UserResponseMapperTests.java`, `UserResponseMapperFactoryTests.java`, `UserResponseTests.java`.
  - Ponto critico arquitetural: controller acessa `UserRepository` diretamente, sem use case.

### 2.9 GET /api/v1/users/{id}

- [x] Contrato
  - Metodo HTTP: `GET`
  - Rota: `/api/v1/users/{id}`
  - Controller: `UserController.getById()`
  - Request DTO: nenhum.
  - Response DTO: `UserListItemResponse`
  - Codigos HTTP possiveis: `200`, `400`, `401`, `403`, `404`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")`
  - Ownership: usuario so acessa o proprio ID; ADMIN acessa qualquer ID.
  - Rate limit: policy default `general`.

- [x] Regras
  - Busca usuario por ID.
  - Responde sempre com `UserListType.DETAILED`.
  - `UserListItemResponse` nao inclui `id`.

- [x] Fluxo completo

```text
UserController.getById()
`-- FindUserByIdUseCase.execute()
    |-- UserRepository.findById()
    `-- UserListItemResponse.from(user, DETAILED)
```

- [x] Arquivos participantes
  - `UserController.java`: `getById`.
  - `FindUserByIdUseCase.java`, `UserListItemResponse.java`, `UserListType.java`.
  - Repositorios/mappers/persistencia comuns.
  - Handlers/security/rate limit comuns.
  - Testes relacionados: `FindUserByIdIT.java`, `FindUserByIdInternalErrorIT.java`, `FindUserByIdUseCaseTests.java`.

### 2.10 GET /api/v1/users

- [x] Contrato
  - Metodo HTTP: `GET`
  - Rota: `/api/v1/users`
  - Controller: `UserController.findAll()`
  - Request DTO: query params `name`, `type`, `Pageable`.
  - Response DTO: `FindAllUsersResponse<UserListItemResponse>`
  - Codigos HTTP possiveis: `200`, `400`, `401`, `403`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Validacoes
  - `name`: `@Size(max = 255)`, `@Pattern("^[\\p{L}\\p{M}0-9 .'-]+$")`.
  - `type`: obrigatorio; enum `SUMMARY` ou `DETAILED`.
  - `Pageable`: `@PageableDefault(size = 20)`.

- [x] Regras
  - Normaliza nome com `trim`, transforma blank em `null`.
  - Busca pagina ativa no repositorio.
  - `SUMMARY`: nome, email, roles.
  - `DETAILED`: inclui status e auditoria.

- [x] Fluxo completo

```text
UserController.findAll()
`-- FindAllUsersUseCase.execute()
    |-- normalizeName()
    |-- UserRepository.findAll(name, pageable)
    |   `-- JpaUserRepositoryAdapter.findAll()
    |       |-- SpringDataUserRepository.findAll(pageable)
    |       `-- SpringDataUserRepository.findByNameContainingIgnoreCase()
    |-- UserListItemResponse.from()
    `-- new FindAllUsersResponse<>()
```

- [x] Arquivos participantes
  - `UserController.java`: `findAll`.
  - `FindAllUsersUseCase.java`, `FindAllUsersResponse.java`, `UserListItemResponse.java`, `UserListType.java`.
  - Repositorios/mappers/persistencia comuns.
  - `GlobalExceptionHandler.java`: query param ausente/invalido e constraint violation.
  - Testes relacionados: `FindAllUsersIT.java`.

### 2.11 PATCH /api/v1/users/me

- [x] Contrato
  - Metodo HTTP: `PATCH`
  - Rota: `/api/v1/users/me`
  - Controller: `UserController.updateProfile()`
  - Request DTO: `UpdateCurrentUserRequest`
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `204`, `400`, `401`, `404`, `409`, `429`, `500`
  - Autenticacao/autorizacao: autenticado por `/api/v1/**`.
  - Ownership: sempre usa `authentication.principal`, nao aceita ID externo.
  - Rate limit: policy default `general`.

- [x] Validacoes
  - `name`: `@Size(max = 255)`, regex contra caracteres fora da allowlist.
  - `email`: `@Email`.
  - `@JsonIgnoreProperties(ignoreUnknown = true)` ignora campos protegidos no payload.

- [x] Regras
  - Busca usuario autenticado.
  - Atualiza nome se informado.
  - Atualiza email se informado e diferente.
  - Nao faz update se nome e email forem omitidos.
  - Duplicidade de email e detectada pelo banco e traduzida para `409`.

- [x] Fluxo completo

```text
UserController.updateProfile()
`-- UpdateProfileUseCase.execute()
    |-- UserRepository.findById()
    |-- resolveNewEmail()
    |   `-- new Email()
    |-- User.setName()
    |-- User.setEmail()
    `-- UserRepository.update()
```

- [x] Arquivos participantes
  - `UserController.java`: `updateProfile`.
  - `UpdateCurrentUserRequest.java`, `UpdateProfileUseCase.java`, `Email.java`.
  - Repositorios/mappers/persistencia comuns.
  - Testes relacionados: `UpdateCurrentUserIT.java`, `UpdateCurrentUserInternalErrorIT.java`, `UpdateProfileUseCaseTests.java`.

### 2.12 DELETE /api/v1/users/{id}

- [x] Contrato
  - Metodo HTTP: `DELETE`
  - Rota: `/api/v1/users/{id}`
  - Controller: `UserController.delete()`
  - Request DTO: nenhum.
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis pelo codigo/testes: `204`, `400`, `401`, `403`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Regras
  - `DeleteUserUseCase.execute()` delega para `UserRepository.deleteById()`.
  - `UserJpaEntity` tem `@SQLDelete`: soft delete via `active=false` e anonimiza email para `id::text || '@deleted.local'`.
  - `@SQLRestriction("active = true")` remove usuarios inativos de consultas JPA padrao.

- [x] Fluxo completo

```text
UserController.delete()
`-- DeleteUserUseCase.execute()
    `-- UserRepository.deleteById()
        `-- JpaUserRepositoryAdapter.deleteById()
            |-- SpringDataUserRepository.deleteById()
            `-- SpringDataUserRepository.flush()
                `-- @SQLDelete on UserJpaEntity
```

- [x] Arquivos participantes
  - `UserController.java`, `DeleteUserUseCase.java`.
  - `UserJpaEntity.java`: soft delete.
  - Repositorios/mappers/persistencia comuns.
  - Testes relacionados: `DeleteUserByIdIT.java`, `DeleteUserByIdInternalErrorIT.java`, `DeleteUserUseCaseTests.java`, `UserRepositoryIT.java`.
  - Ponto critico: use case nao verifica existencia antes de deletar; contrato de 404 para usuario inexistente nao esta explicito no codigo.

### 2.13 PATCH /api/v1/users/{id}/unlock

- [x] Contrato
  - Metodo HTTP: `PATCH`
  - Rota: `/api/v1/users/{id}/unlock`
  - Controller: `UserController.unlock()`
  - Request DTO: nenhum.
  - Response DTO: nenhum corpo.
  - Codigos HTTP possiveis: `204`, `400`, `401`, `403`, `404`, `429`, `500`
  - Autenticacao/autorizacao: `@PreAuthorize("hasRole('ADMIN')")`
  - Rate limit: policy default `general`.

- [x] Regras
  - Busca usuario por ID.
  - Executa `User.unlock()`.
  - Remove contador Redis `login_attempts:<email>`.
  - Persiste status.

- [x] Fluxo completo

```text
UserController.unlock()
`-- UnlockUserUseCase.execute()
    |-- UserRepository.findById()
    |-- User.unlock()
    |-- StringRedisTemplate.delete("login_attempts:" + email)
    `-- UserRepository.update()
```

- [x] Arquivos participantes
  - `UserController.java`, `UnlockUserUseCase.java`, `User.java`.
  - Repositorios/mappers/persistencia comuns.
  - `StringRedisTemplate` como dependencia externa.
  - Testes relacionados: `UnlockUserIT.java`, `UnlockUserInternalErrorIT.java`, `UnlockUserUseCaseTests.java`.

## 3. Fluxos internos que nao sao endpoints

- [x] Filtro de autenticacao JWT
  - Arquivo: `JwtAuthenticationFilter.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\JwtAuthenticationFilter.java`
  - Classe/metodo: `JwtAuthenticationFilter.doFilterInternal()`
  - Arquivos relacionados: `JwtService.java`, `TokenService.java`, `SecurityConfig.java`.
  - Papel: traduz cookies em `SecurityContext`.

- [x] Filtro de rate limit
  - Arquivo: `RateLimitFilter.java`
  - Classe/metodo: `RateLimitFilter.doFilterInternal()`
  - Arquivos relacionados: `UserRateLimitConfig.java`, `UserRateLimitProperties.java`, `RateLimitDecision.java`, `RateLimitTimeConfig.java`, properties.
  - Papel: limita requests por IP e policy.

- [x] Cache/estado Redis de refresh tokens
  - Arquivo: `TokenService.java`
  - Metodos: `generateRefreshToken`, `validateRefreshToken`, `getUserIdFromRefreshToken`, `consumeRefreshToken`, `revokeRefreshToken`.
  - Chaves: `refresh_token:<hmac-do-token>`.
  - Dados armazenados: `userId.toString()`, com TTL configurado por `stockfy.security.jwt.refresh-token-expiration`.
  - Ponto critico: apenas o refresh token atual e revogado no logout; nao ha invalidacao global por usuario.

- [x] Cache/estado Redis de tentativas de login
  - Arquivo: `LoginUseCase.java`
  - Metodos: `handleFailedLogin`, `resetFailedAttempts`.
  - Chave: `login_attempts:<email>`, TTL 1 dia.
  - Side effects: bloqueio de usuario e publish RabbitMQ.

- [x] Cache/estado Redis de tentativas de senha atual
  - Arquivo: `UpdatePasswordUseCase.java`
  - Metodo: `countInvalidAttempt`.
  - Chave: `password_update_attempts:<userId>`, TTL 5 minutos.
  - Side effects: bloqueio de usuario apos 15 falhas.

- [x] Evento publicado em RabbitMQ
  - Evento: `AccountLockedEvent`
  - Arquivo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\event\AccountLockedEvent.java`
  - Publisher: `RabbitMqEventPublisher.publish()`
  - Caminho completo do publisher: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\messaging\RabbitMqEventPublisher.java`
  - Exchange/routing key: `security.exchange` / `account.locked`
  - Config relacionada: `RabbitMqConfig.jsonMessageConverter()`
  - Ponto critico: nao ha declaracao de exchange, queue ou binding no codigo; tambem nao ha consumer.

- [x] Auditoria JPA/Envers
  - Arquivo: `UserJpaEntity.java`
  - Classe/metodo: anotacoes de classe e campos auditados.
  - Arquivos relacionados: `JpaConfig.java`, `AuditorAwareImpl.java`, migrations `V1__create_users_table.sql`.
  - Tabelas: `users_aud`, `users_roles_aud`, `revinfo`.
  - Papel: historico de mutacoes de usuario e roles.

- [x] Soft delete
  - Arquivo: `UserJpaEntity.java`
  - Trecho responsavel: `@SQLDelete(sql = "UPDATE users SET active = false, email = id::text || '@deleted.local' WHERE id = ?")`, `@SQLRestriction("active = true")`.
  - Ponto critico: soft delete nao revoga tokens existentes no Redis.

- [x] Mensageria, schedulers, listeners, jobs, uploads, webhooks
  - Consumers/listeners: nao encontrados em `src/main/java` via busca por `@RabbitListener`, `@EventListener`.
  - Schedulers/jobs async: nao encontrados via `@Scheduled`, `@Async`.
  - Uploads/webhooks: nao encontrados via `Multipart`, `upload`, `Webhook`.
  - Spring cache: nao encontrado via `@Cacheable`; caches efetivos sao Redis manual e maps Bucket4j.

## 4. Autenticacao e autorizacao

- [x] Roles existentes
  - Arquivo: `UserRole.java`
  - Caminho completo: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\UserRole.java`
  - Classe: `UserRole`
  - Valores: `ADMIN`, `USER`
  - Ponto critico: nao ha modelo de permissoes granulares alem de roles.

- [x] Permissoes por endpoint
  - Publico: `POST /api/v1/auth/sessions`, `POST /api/v1/auth/sessions/refresh`, `PATCH /api/v1/users/password` com regra de metodo.
  - Autenticado: `DELETE /api/v1/auth/sessions/current`, `GET /api/v1/users/me`, `PATCH /api/v1/users/me`.
  - ADMIN: `POST /api/v1/users`, `PATCH /api/v1/users/{id}/roles`, `POST /api/v1/users/{id}/password-reset-codes`, `GET /api/v1/users`, `DELETE /api/v1/users/{id}`, `PATCH /api/v1/users/{id}/unlock`.
  - ADMIN ou owner: `GET /api/v1/users/{id}`.

- [x] Validacoes de ownership
  - `UserController.getById()`: `@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")`.
  - `UserController.me()`: usa `authentication.principal` como ID.
  - `UserController.updateProfile()`: usa `authentication.principal` como ID.
  - `UserController.updatePassword()`: fluxo autenticado usa `authentication.principal`; fluxo com reset code nao exige ownership.

- [x] Verificacoes de sessao/token
  - `JwtAuthenticationFilter.doFilterInternal()`: access token precisa ser valido, refresh token precisa existir no Redis, e ambos precisam apontar para o mesmo UUID.
  - `RefreshTokenUseCase.execute()`: refresh token precisa existir, ser consumido atomicamente, e usuario precisa existir/estar ativo/desbloqueado.
  - `LogoutUseCase.execute()`: remove refresh token atual.

## 5. Persistencia

- [x] Tabelas principais
  - `users`
    - Migration: `C:\Projetos\stockfy\stockfy-server\src\main\resources\db\migration\V1__create_users_table.sql`
    - Entidade: `UserJpaEntity`
    - PK: `id UUID PRIMARY KEY`
    - Constraints: `email UNIQUE NOT NULL`, `reset_password_code_hash UNIQUE`, `password_hash NOT NULL`, `status NOT NULL`, `active NOT NULL`.
    - Campos auditaveis: `created_at`, `created_by`, `updated_at`, `updated_by`.
  - `users_roles`
    - Relacionamento: element collection de roles.
    - PK composta: `(user_id, role)`.
    - FK: `user_id REFERENCES users(id)`.
  - `revinfo`, `users_aud`, `users_roles_aud`
    - Criadas para Hibernate Envers.
    - `users_aud` referencia `revinfo`.
    - `users_roles_aud` referencia `revinfo`.

- [x] Entidades e relacionamentos
  - Dominio: `User`, `Email`, `Password`, `UserRole`, `UserStatus`.
  - Persistencia: `UserJpaEntity`.
  - Relacionamento roles: `@ElementCollection(fetch = FetchType.EAGER)` em `users_roles`.
  - Mapper: `UserPersistenceMapper.toEntity()` e `toDomain()`.

- [x] Transacoes
  - Mutacao: `RegisterUserUseCase.execute`, `UpdateProfileUseCase.execute`, `UpdatePasswordUseCase.executeWithCode`, `UpdatePasswordUseCase.executeAuthenticated`, `GenerateResetCodeUseCase.execute`, `DeleteUserUseCase.execute`, `UnlockUserUseCase.execute`, `UpdateUserRolesUseCase.execute`, `LoginUseCase.execute`.
  - Leitura: `FindAllUsersUseCase.execute`, `FindUserByIdUseCase.execute`, `RefreshTokenUseCase.execute`.
  - Ponto critico: `RefreshTokenUseCase.execute` e read-only, mas executa efeitos Redis.

- [x] Soft delete
  - `UserJpaEntity.@SQLDelete`: atualiza `active=false` e anonimiza email.
  - `UserJpaEntity.@SQLRestriction`: filtra `active=true`.
  - Teste relacionado: `UserRepositoryIT.deleteById_whenCalled_thenAnonymizesEmailAndSoftDeletesUser`.

- [x] Auditoria
  - `UserJpaEntity.@Audited`, campos `@CreatedDate`, `@CreatedBy`, `@LastModifiedDate`, `@LastModifiedBy`.
  - `JpaConfig.@EnableJpaAuditing`.
  - `AuditorAwareImpl.getCurrentAuditor()` usa principal UUID do `SecurityContext`.
  - Testes relacionados: `UserAuditIT.userMutations_whenPerformed_thenCreatesAuditHistoryInUsersAud`, `UserAuditIT.userRoleMutation_whenPerformed_thenCreatesAuditHistoryInUsersRolesAud`.

## 6. Fluxos criticos de seguranca

- [x] Entrada e validacao
  - Evidencia:
    - DTOs: `LoginRequest`, `RegisterUserRequest`, `UpdatePasswordRequest`, `UpdateCurrentUserRequest`, `UpdateUserRolesRequest`.
    - Query param: `UserController.findAll()` valida `name` com size e pattern.
    - Handler: `GlobalExceptionHandler` converte invalidacoes para `400`.
  - Justificativa tecnica: validacao ocorre antes do use case com Jakarta Validation e, em erro, gera `ApiErrorResponse` com `fieldErrors`.
  - Ponto critico: `RegisterUserRequest.password` so tem `@NotBlank`; minimo 8 vem do VO `Password` e vira `422`, nao `400`.

- [x] Rate limit
  - Evidencia:
    - `SecurityConfig.securityFilterChain()` registra `RateLimitFilter` antes do filtro de autenticacao.
    - `UserRateLimitConfig.consume()` usa Bucket4j com `ConcurrentHashMap`.
    - Properties definem policies `general`, `login`, `password`, `refresh`, `logout`.
  - Justificativa tecnica: cada request consome bucket por `policyName:clientIp`.
  - Pontos criticos:
    - Nao distribuido entre instancias.
    - Baseado em IP remoto bruto.
    - `refresh.blockCapacity` e consumido em toda requisicao refresh, nao apenas em refresh rejeitado.

- [x] Protecao contra enumeracao
  - Evidencia:
    - `LoginUseCase.execute()` converte email inexistente, usuario bloqueado/inativo e senha errada em `InvalidCredentialsException`.
    - `UserExceptionHandler.handleInvalidCredentialsException()` retorna `401` sem corpo.
  - Justificativa tecnica: login nao diferencia usuario inexistente, bloqueado/inativo ou senha errada na resposta HTTP.
  - Ponto critico: endpoint admin de reset code e endpoints autenticados podem revelar existencia de ID para usuarios autorizados via `404`.

- [x] Exposicao de dados sensiveis
  - Evidencia:
    - `UserResponse` nao tem campo de senha.
    - `UserListItemResponse` nao tem campo de senha.
    - `AuthController.login()` e `refresh()` retornam `ResponseEntity<Void>`, nao `AuthResponse` no corpo.
    - `AuthController.addCookies()` define `HttpOnly` e `Secure`.
  - Justificativa tecnica: senha/hash nao fazem parte dos DTOs de saida e tokens sao transportados via cookies.
  - Pontos criticos:
    - Cookies nao configuram `SameSite`.
    - `AccountLockedEvent` carrega email em RabbitMQ.
    - `POST /password-reset-codes` retorna o codigo no body para ADMIN.

- [x] Fluxo de tokens
  - Evidencia:
    - `JwtService.generateToken()` assina JWT com subject userId e claim `roles`.
    - `TokenService.generateRefreshToken()` gera UUID aleatorio, armazena HMAC no Redis com TTL.
    - `RefreshTokenUseCase.execute()` consome refresh token e emite outro.
  - Justificativa tecnica: refresh token bruto nao e usado como chave direta, e rotacao usa `getAndDelete`.
  - Pontos criticos:
    - Nao ha revogacao global por usuario.
    - Troca de senha e soft delete nao revogam tokens ja existentes.
    - Access token nao e persistido, entao so expira por tempo.

- [x] Criptografia/hash
  - Senha: `SecurityConfig.passwordEncoder()` retorna `BCryptPasswordEncoder`.
  - Reset/refresh token key: `HmacSha256Hasher.hash()` usa `HmacSHA256` com `stockfy.security.hmac.secret`.
  - JWT: `JwtService.getSigningKey()` usa `stockfy.security.jwt.secret`.
  - Ponto critico: dev/test tem segredos hardcoded; prod usa variaveis.

## 7. Dependencias externas

- [x] PostgreSQL
  - Usado por Spring Data JPA/Flyway.
  - Arquivos: `build.gradle`, `application-*.properties`, `V1__create_users_table.sql`, `V2__insert_initial_admin.sql`, `UserJpaEntity.java`, repositories.
  - Testes: Testcontainers em `ContainersConfiguration.postgresContainer()`.

- [x] Redis
  - Usado por refresh tokens e contadores de tentativas.
  - Arquivos: `TokenService.java`, `LoginUseCase.java`, `UpdatePasswordUseCase.java`, `UnlockUserUseCase.java`, `application-*.properties`.
  - Testes: Testcontainers em `ContainersConfiguration.redisContainer()`.

- [x] RabbitMQ
  - Usado apenas como publisher de evento `AccountLockedEvent`.
  - Arquivos: `RabbitMqEventPublisher.java`, `RabbitMqConfig.java`, `build.gradle`, `compose.yaml`.
  - Testes: `RabbitMqConfigTests.java`, Testcontainers em `ContainersConfiguration.rabbitContainer()`.
  - Ponto critico: nao ha consumer, exchange, queue ou binding declarados no codigo.

- [x] Bibliotecas externas relevantes
  - Spring Security: filtros, roles, method security.
  - JJWT: assinatura e parsing de JWT.
  - Bucket4j: rate limit.
  - Hibernate Envers: auditoria.
  - MapStruct: mapeamento DTO.
  - BCrypt: hash de senha.

- [x] Ausentes no modulo
  - APIs externas de terceiros: nao encontradas.
  - Email/SMS: nao encontrado; reset code e retornado via HTTP.
  - Arquivos/uploads: nao encontrados.
  - Webhooks: nao encontrados.

## 8. Casos extremos e pontos frageis

- [ ] CSRF com cookies
  - Arquivos/metodos: `SecurityConfig.securityFilterChain()`, `AuthController.addCookies()`.
  - Evidencia: CSRF desabilitado e cookies HttpOnly/Secure sem SameSite.
  - Risco: endpoints com cookies podem ser acionados cross-site se o browser enviar cookies.

- [ ] Rate limit por IP local e in-memory
  - Arquivos/metodos: `RateLimitFilter.doFilterInternal()`, `UserRateLimitConfig.consume()`.
  - Risco: multi-instancia nao compartilha buckets; proxy pode agrupar usuarios pelo mesmo IP.

- [ ] RabbitMQ publish dentro do login failure path
  - Arquivos/metodos: `LoginUseCase.handleFailedLogin()`, `RabbitMqEventPublisher.publish()`.
  - Risco: falha de RabbitMQ pode transformar tentativa invalida em erro interno e afetar bloqueio/publicacao.

- [ ] Reset code tem espaco de 1.000.000 possibilidades
  - Arquivos/metodos: `GenerateResetCodeUseCase.execute()`, `UserJpaEntity.resetPasswordCodeHash`.
  - Risco: colisao de hash no unique constraint pode virar erro interno, pois adapter so traduz constraint de email.

- [ ] Troca de senha nao revoga sessoes
  - Arquivos/metodos: `UpdatePasswordUseCase.executeWithCode()`, `UpdatePasswordUseCase.executeAuthenticated()`, `TokenService`.
  - Risco: refresh tokens ja emitidos continuam validos ate TTL ou logout individual.

- [ ] Soft delete nao revoga tokens
  - Arquivos/metodos: `DeleteUserUseCase.execute()`, `UserJpaEntity.@SQLDelete`, `RefreshTokenUseCase.execute()`.
  - Atenuante: refresh valida `user.isActive()` ao buscar usuario; access token sem refresh nao autentica pelo filtro atual.
  - Risco residual: tokens continuam no Redis ate expirar.

- [ ] `RefreshTokenUseCase` usa transacao read-only com efeito em Redis
  - Arquivo/metodo: `RefreshTokenUseCase.execute()`.
  - Risco: sem impacto JPA direto, mas semantica transacional fica enganosa.

- [ ] `UserController.me()` acessa repository direto
  - Arquivo/metodo: `UserController.me()`.
  - Risco: desvio da arquitetura hexagonal/use case, regra de apresentacao no controller.

- [ ] `GET /api/v1/users/{id}` usa `UserListItemResponse`, nao `UserResponse`
  - Arquivo/metodo: `UserController.getById()`, `FindUserByIdUseCase.execute()`.
  - Risco: contrato de resposta difere do AGENTS local que menciona `UserResponse`.

- [ ] Role model e apenas RBAC simples
  - Arquivos: `UserRole.java`, `UserController.java`.
  - Risco: AGENTS local menciona permissoes granulares, mas codigo so usa `ADMIN`/`USER`.

- [ ] Sem handlers customizados para `MissingRequestCookieException` e erro interno generico
  - Arquivos: `GlobalExceptionHandler.java`, `UserExceptionHandler.java`.
  - Risco: alguns erros ficam no comportamento padrao do Spring Boot.

## 9. Inventario completo de arquivos de producao participantes

### 9.1 Controllers e presentation

- [x] `AuthController.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\controller\AuthController.java`
  - Classe: `AuthController`
  - Metodos: `login`, `refresh`, `logout`, `addCookies`, `clearCookies`, `expiredCookie`
  - Responsabilidade: endpoints de sessao e cookies.
  - Papel no fluxo: entrada HTTP para login, refresh e logout.
  - Riscos: cookies sem `SameSite`; tokens nunca retornam no body, apenas cookies.

- [x] `UserController.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\controller\UserController.java`
  - Classe: `UserController`
  - Metodos: `register`, `updateUserRoles`, `generateResetCode`, `updatePassword`, `me`, `getById`, `findAll`, `updateProfile`, `delete`, `unlock`
  - Responsabilidade: endpoints de usuarios.
  - Papel no fluxo: recebe DTOs/path/query/authentication e chama use cases.
  - Riscos: `me()` usa repository direto; `PATCH /password` mistura fluxo publico por codigo e autenticado.

- [x] `UserExceptionHandler.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\UserExceptionHandler.java`
  - Classe: `UserExceptionHandler`
  - Metodos: handlers de excecoes users.
  - Responsabilidade: traduz excecoes do modulo para HTTP.
  - Riscos: 401 sem corpo esta correto pelo contrato atual; demais erros usam body `ApiErrorResponse`.

- [x] `UserResponseMapper.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\mapper\UserResponseMapper.java`
  - Classe: `UserResponseMapper`
  - Metodos: `toFullResponse`, `toOwnerResponse`, `toPublicResponse`, `emptyRoles`
  - Responsabilidade: MapStruct para resposta de perfil.
  - Riscos: mapeamento publico ainda retorna `id` e `name`, roles vazio; email oculto.

- [x] `UserResponseMapperFactory.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\presentation\mapper\UserResponseMapperFactory.java`
  - Classe: `UserResponseMapperFactory`
  - Metodo: `toResponse`
  - Responsabilidade: seleciona resposta por role/ownership.
  - Riscos: assume `Authentication` e principal `UUID` sempre presentes.

### 9.2 DTOs e validadores

- [x] `LoginRequest.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\LoginRequest.java`
  - Classe: `LoginRequest`
  - Metodos: record accessors `email`, `password`
  - Responsabilidade: entrada de login.
  - Riscos: nenhum campo extra por ser record.

- [x] `AuthResponse.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\AuthResponse.java`
  - Classe: `AuthResponse`
  - Metodos: record accessors `accessToken`, `refreshToken`
  - Responsabilidade: transporte interno de tokens ate controller.
  - Riscos: se retornado no body em mudanca futura exporia tokens; hoje controllers retornam `Void`.

- [x] `RegisterUserRequest.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\RegisterUserRequest.java`
  - Classe: `RegisterUserRequest`
  - Metodos: record accessors.
  - Responsabilidade: entrada de cadastro admin.
  - Riscos: `confirmPassword` sem annotation; regra fica no use case.

- [x] `UpdatePasswordRequest.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UpdatePasswordRequest.java`
  - Classe: `UpdatePasswordRequest`
  - Metodos: record accessors.
  - Responsabilidade: entrada de reset/troca de senha.
  - Riscos: `currentPassword` opcional permite dois fluxos no mesmo DTO.

- [x] `UpdateCurrentUserRequest.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UpdateCurrentUserRequest.java`
  - Classe: `UpdateCurrentUserRequest`
  - Metodos: record accessors.
  - Responsabilidade: entrada para atualizar perfil proprio.
  - Riscos: campos desconhecidos sao ignorados; isso evita protected-field mass assignment, mas pode ocultar erro de cliente.

- [x] `UpdateUserRolesRequest.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UpdateUserRolesRequest.java`
  - Classe: `UpdateUserRolesRequest`
  - Metodos: compact constructor, `add`, `remove`, `rolesToAdd`, `rolesToRemove`, `toRoles`, `copyNullableList`
  - Responsabilidade: entrada de alteracao de roles.
  - Riscos: `UserRole.valueOf` e chamado apos validacao; se bypassado por uso direto com valor invalido, pode lancar `IllegalArgumentException`.

- [x] `UserResponse.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UserResponse.java`
  - Classe: `UserResponse`
  - Metodos: compact constructor, `roles`
  - Responsabilidade: resposta de perfil.
  - Riscos: inclui auditoria para resposta full.

- [x] `UserListItemResponse.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UserListItemResponse.java`
  - Classe: `UserListItemResponse`
  - Metodos: compact constructor, `roles`, `from`, `summary`, `detailed`
  - Responsabilidade: item de lista/detalhe por tipo.
  - Riscos: `DETAILED` inclui auditoria; nao inclui `id`.

- [x] `FindAllUsersResponse.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\FindAllUsersResponse.java`
  - Classe: `FindAllUsersResponse`
  - Metodos: compact constructor, `content`
  - Responsabilidade: envelope de pagina.
  - Riscos: nenhum relevante.

- [x] `UserListType.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\dto\UserListType.java`
  - Classe: `UserListType`
  - Valores: `SUMMARY`, `DETAILED`
  - Responsabilidade: seleciona formato de item.
  - Riscos: query param invalido vira `400`.

- [x] `ValidUserRole.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\validation\ValidUserRole.java`
  - Classe: annotation `ValidUserRole`
  - Responsabilidade: constraint de role.
  - Riscos: depende do validator abaixo.

- [x] `ValidUserRoleValidator.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\validation\ValidUserRoleValidator.java`
  - Classe: `ValidUserRoleValidator`
  - Metodo: `isValid`
  - Responsabilidade: valida string contra enum `UserRole`.
  - Riscos: case-sensitive.

### 9.3 Use cases

- [x] `LoginUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\LoginUseCase.java`
  - Classe: `LoginUseCase`
  - Metodos: `execute`, `handleFailedLogin`, `resetFailedAttempts`, `parseEmail`
  - Responsabilidade: autenticacao, lock e tokens.
  - Riscos: RabbitMQ no fluxo de erro; contador Redis por email.

- [x] `RefreshTokenUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\RefreshTokenUseCase.java`
  - Classe: `RefreshTokenUseCase`
  - Metodos: `execute`, `consumeRefreshToken`
  - Responsabilidade: rotacao de refresh token.
  - Riscos: `@Transactional(readOnly = true)` com Redis mutation.

- [x] `LogoutUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\LogoutUseCase.java`
  - Classe: `LogoutUseCase`
  - Metodo: `execute`
  - Responsabilidade: revogar refresh token atual.
  - Riscos: nao revoga todos os tokens do usuario.

- [x] `RegisterUserUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\RegisterUserUseCase.java`
  - Classe: `RegisterUserUseCase`
  - Metodo: `execute`
  - Responsabilidade: cadastro admin.
  - Riscos: nao preconsulta email por design; conflito vem do banco.

- [x] `FindAllUsersUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\FindAllUsersUseCase.java`
  - Classe: `FindAllUsersUseCase`
  - Metodos: `execute`, `normalizeName`
  - Responsabilidade: listagem paginada.
  - Riscos: filtro depende de query derivada JPA.

- [x] `FindUserByIdUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\FindUserByIdUseCase.java`
  - Classe: `FindUserByIdUseCase`
  - Metodo: `execute`
  - Responsabilidade: detalhe por ID.
  - Riscos: retorna `UserListItemResponse`, nao `UserResponse`.

- [x] `UpdateProfileUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\UpdateProfileUseCase.java`
  - Classe: `UpdateProfileUseCase`
  - Metodos: `execute`, `resolveNewEmail`
  - Responsabilidade: atualizar nome/email proprio.
  - Riscos: conflito de email detectado so no flush.

- [x] `UpdatePasswordUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\UpdatePasswordUseCase.java`
  - Classe: `UpdatePasswordUseCase`
  - Metodos: `executeWithCode`, `executeAuthenticated`, `validatePasswordConfirmation`, `findUserByResetCode`, `resetCodeNotExpired`, `updatePassword`, `countInvalidAttempt`
  - Responsabilidade: reset por codigo e troca autenticada.
  - Riscos: nao revoga tokens depois da troca.

- [x] `GenerateResetCodeUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\GenerateResetCodeUseCase.java`
  - Classe: `GenerateResetCodeUseCase`
  - Metodo: `execute`
  - Responsabilidade: gerar codigo de recuperacao.
  - Riscos: retorna codigo no HTTP; colisao de 6 digitos possivel.

- [x] `DeleteUserUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\DeleteUserUseCase.java`
  - Classe: `DeleteUserUseCase`
  - Metodo: `execute`
  - Responsabilidade: soft delete por ID.
  - Riscos: nao revoga tokens e nao valida existencia explicitamente.

- [x] `UnlockUserUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\UnlockUserUseCase.java`
  - Classe: `UnlockUserUseCase`
  - Metodo: `execute`
  - Responsabilidade: desbloquear conta e limpar tentativas de login.
  - Riscos: limpa por email atual; se email mudou, tentativas antigas podem permanecer.

- [x] `UpdateUserRolesUseCase.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\usecase\UpdateUserRolesUseCase.java`
  - Classe: `UpdateUserRolesUseCase`
  - Metodo: `execute`
  - Responsabilidade: adicionar/remover roles.
  - Riscos: depende da invariante `User.updateRoles`.

### 9.4 Dominio

- [x] `User.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\User.java`
  - Classe: `User`
  - Metodos: `lock`, `unlock`, `deactivate`, `activate`, `updateRoles`
  - Responsabilidade: agregado de usuario.
  - Riscos: setters publicos permitem mutacao fora de metodos ricos.

- [x] `Email.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\Email.java`
  - Classe: `Email`
  - Metodo: canonical constructor.
  - Responsabilidade: VO de email.
  - Riscos: regex rejeita TLDs maiores que 6 caracteres.

- [x] `Password.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\Password.java`
  - Classe: `Password`
  - Metodo: canonical constructor.
  - Responsabilidade: VO de senha/hash.
  - Riscos: usado tambem para hash; comentario no mapper indica acoplamento conceitual.

- [x] `UserRole.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\UserRole.java`
  - Classe: enum `UserRole`
  - Responsabilidade: roles RBAC.
  - Riscos: sem permissoes granulares.

- [x] `UserStatus.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\model\UserStatus.java`
  - Classe: enum `UserStatus`
  - Responsabilidade: status `ACTIVE`/`LOCKED`.
  - Riscos: nao ha status intermediario para reset/revogado.

- [x] `AccountLockedEvent.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\event\AccountLockedEvent.java`
  - Classe: `AccountLockedEvent`
  - Responsabilidade: evento de conta bloqueada.
  - Riscos: carrega email em evento externo.

- [x] `UserRepository.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\repository\UserRepository.java`
  - Classe: interface `UserRepository`
  - Metodos: `save`, `findById`, `findByEmail`, `findByResetPasswordCodeHash`, `findAll`, `update`, `deleteById`
  - Responsabilidade: porta de persistencia.
  - Riscos: contrato `deleteById` nao declara comportamento para ID ausente.

- [x] `InvalidUserRolesException.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\domain\exception\InvalidUserRolesException.java`
  - Classe: `InvalidUserRolesException`
  - Responsabilidade: regra de roles vazias.
  - Riscos: mapeada como `400`.

### 9.5 Infraestrutura de persistencia

- [x] `UserJpaEntity.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\persistence\UserJpaEntity.java`
  - Classe: `UserJpaEntity`
  - Responsabilidade: entidade JPA `users`.
  - Metodos: Lombok accessors/builders.
  - Riscos: soft delete nao apaga roles; apenas oculta usuario ativo via restriction.

- [x] `SpringDataUserRepository.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\persistence\SpringDataUserRepository.java`
  - Classe: `SpringDataUserRepository`
  - Metodos: `findByEmail`, `findByResetPasswordCodeHash`, `findAllByName`, `findByNameContainingIgnoreCase`
  - Responsabilidade: adapter Spring Data.
  - Riscos: query derivada depende de escaping do provider; validacao de `name` reduz risco de entrada maliciosa.

- [x] `JpaUserRepositoryAdapter.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\persistence\JpaUserRepositoryAdapter.java`
  - Classe: `JpaUserRepositoryAdapter`
  - Metodos: todos os metodos da porta, `translateConstraintViolation`
  - Responsabilidade: implementa `UserRepository`.
  - Riscos: traduz apenas constraints de email; reset code duplicado pode vazar como erro interno.

- [x] `UserPersistenceMapper.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\persistence\UserPersistenceMapper.java`
  - Classe: `UserPersistenceMapper`
  - Metodos: `toEntity`, `toDomain`, `toEmail`, `toPassword`
  - Responsabilidade: MapStruct dominio/JPA.
  - Riscos: `Password` VO embrulha hash.

### 9.6 Infraestrutura de seguranca, config e messaging

- [x] `TokenService.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\TokenService.java`
  - Classe: `TokenService`
  - Metodos: `generateRefreshToken`, `validateRefreshToken`, `getUserIdFromRefreshToken`, `consumeRefreshToken`, `revokeRefreshToken`
  - Responsabilidade: refresh tokens em Redis.
  - Riscos: comentario e indentacao do metodo `validateRefreshToken` estao desalinhados; funcionalmente usa `Boolean.TRUE.equals` para Boolean nullable.

- [x] `JwtService.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\JwtService.java`
  - Classe: `JwtService`
  - Metodos: `generateToken`, `isTokenValid`, `extractUserId`, `extractRoles`, privados de claim/key.
  - Responsabilidade: JWT access token.
  - Riscos: sem token version/session id para invalidacao seletiva.

- [x] `HmacSha256Hasher.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security\HmacSha256Hasher.java`
  - Classe: `HmacSha256Hasher`
  - Metodo: `hash`
  - Responsabilidade: HMAC para refresh/reset code.
  - Riscos: falha de algoritmo/chave vira `IllegalStateException`.

- [x] `RateLimitFilter.java`, `JwtAuthenticationFilter.java`, `UserRateLimitConfig.java`, `UserRateLimitProperties.java`, `RateLimitDecision.java`, `RateLimitTimeConfig.java`
  - Caminhos: sob `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\security` e `...\users\infrastructure\config`.
  - Responsabilidade: rate limit, autenticacao por cookie/token e configuracao Bucket4j.
  - Riscos: ja listados em seguranca.

- [x] `RabbitMqConfig.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\messaging\RabbitMqConfig.java`
  - Classe: `RabbitMqConfig`
  - Metodo: `jsonMessageConverter`
  - Responsabilidade: conversor JSON.
  - Riscos: nao declara exchange/queue/binding.

- [x] `RabbitMqEventPublisher.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\infrastructure\messaging\RabbitMqEventPublisher.java`
  - Classe: `RabbitMqEventPublisher`
  - Metodo: `publish`
  - Responsabilidade: publica `AccountLockedEvent`.
  - Riscos: publish sincrono no fluxo de login.

- [x] `PasswordResetProperties.java`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\users\application\config\PasswordResetProperties.java`
  - Classe: `PasswordResetProperties`
  - Responsabilidade: config de expiracao de codigo.

- [x] `SecurityConfig.java`, `SecurityProperties.java`, `UnauthorizedAuthenticationEntryPoint.java`, `ForbiddenAccessDeniedResponder.java`, `JpaConfig.java`, `AuditorAwareImpl.java`, `LocaleConfig.java`
  - Caminhos: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\config\*.java`
  - Responsabilidade: cross-cutting security, auth errors, auditing, locale.
  - Riscos: CSRF desabilitado; auditor depende de principal UUID.

- [x] `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `HttpMessageNotReadableError.java`, `HttpMessageNotReadableErrorFactory.java`
  - Caminhos: `C:\Projetos\stockfy\stockfy-server\src\main\java\br\com\threadstech\stockfy\shared\exception\*.java`
  - Responsabilidade: erros globais.
  - Riscos: sem handler custom generico para 500.

### 9.7 Resources e migrations

- [x] `application.properties`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\application.properties`
  - Papel: profile default `prod`, exclui auto-config default de user details, alias de secret reset.

- [x] `application-dev.properties`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\application-dev.properties`
  - Papel: segredos dev, rate limit, Redis e PostgreSQL local.
  - Riscos: segredos hardcoded em dev.

- [x] `application-prod.properties`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\application-prod.properties`
  - Papel: env vars prod, `server.error.include-stacktrace=never`.
  - Riscos: app falha se env vars obrigatorias ausentes.

- [x] `messages.properties`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\messages.properties`
  - Papel: i18n de validacao/excecoes.
  - Riscos: algumas mensagens estao sem acentos por encoding atual.

- [x] `V1__create_users_table.sql`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\db\migration\V1__create_users_table.sql`
  - Papel: cria users, roles e auditoria.
  - Riscos: sem indices explicitos alem de PK/unique.

- [x] `V2__insert_initial_admin.sql`
  - Caminho: `C:\Projetos\stockfy\stockfy-server\src\main\resources\db\migration\V2__insert_initial_admin.sql`
  - Papel: seed de admin inicial.
  - Riscos: email e hash fixos no seed.

## 10. Testes relacionados

- [x] Testes de endpoint/controller
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\modules\users\presentation\controller\LoginUserIT.java`: sucesso, validacao, 401, lock, 429, IP separado.
  - `...\LoginUserInternalErrorIT.java`: 500 sem stacktrace.
  - `...\RefreshTokenIT.java`: rotacao, missing/invalid/blank/revoked/nonexistent/malformed/locked/inactive, rate limit.
  - `...\RefreshTokenInternalErrorIT.java`: 500 sem stacktrace.
  - `...\LogoutUserIT.java`: revogacao, cookies, 401, 429.
  - `...\LogoutUserInternalErrorIT.java`: 500 sem stacktrace.
  - `...\UserCreationIT.java`: 201, authz, validacao, protected fields, SQL injection, 429.
  - `...\FindAllUsersIT.java`: summary/detailed, filtro, paginacao, type invalid/missing, authz, SQL injection, 429.
  - `...\FindUserByIdIT.java`: owner/admin, 403, 401, UUID invalido, 404, 429.
  - `...\DeleteUserByIdIT.java`: soft delete, login bloqueado apos delete, authz, UUID invalido, 429.
  - `...\GenerateResetCodeIT.java`: hash/expiracao, substituicao de codigo, authz, UUID invalido, 429.
  - `...\UpdatePasswordIT.java`: reset code, reuse/expired, autenticado, invalid current password, lock, 429.
  - `...\UpdateCurrentUserIT.java`: update parcial, protected fields, auth, validacao, conflito, 429.
  - `...\UnlockUserIT.java`: desbloqueio, limpa Redis, authz, UUID invalido, 429.
  - `...\UpdateUserRolesIT.java`: add/remove/protected fields/authz/validacao/roles vazias/429.
  - `...\UserControllerTests.java`, `...\AuthControllerTests.java`: testes MVC focados.
  - `...\*InternalErrorIT.java`: cobrem erro inesperado por endpoint principal.

- [x] Testes de use case/dominio/infra
  - `AuthenticationTests.java`: login, refresh, logout, invalid credentials, inactive/locked.
  - `UserManagementTests.java`: register/update/delete basicos e duplicidade por repositorio.
  - `UpdateProfileUseCaseTests.java`, `UpdatePasswordUseCaseTests.java`, `UpdateUserRolesUseCaseTests.java`, `UnlockUserUseCaseTests.java`, `PasswordResetTests.java`, `FindUserByIdUseCaseTests.java`, `DeleteUserUseCaseTests.java`.
  - `UserTests.java`, `EmailTests.java`, `PasswordTests.java`, `UserEnumsTests.java`, `AccountLockedEventTests.java`.
  - `UserRepositoryIT.java`, `JpaUserRepositoryAdapterTests.java`, `UserAuditIT.java`.
  - `TokenServiceTests.java`, `JwtAuthenticationFilterTests.java`, `RateLimitFilterTests.java`, `HmacSha256HasherTests.java`, `UserRateLimitConfigTests.java`.
  - `RabbitMqConfigTests.java`.
  - `UserResponseMapperTests.java`, `UserResponseMapperFactoryTests.java`, `UserResponseTests.java`.
  - `ValidUserRoleValidatorTests.java`, `UserExceptionMessageKeyTests.java`.

- [x] Test support e fixtures
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\ContainersConfiguration.java`: Testcontainers PostgreSQL, RabbitMQ, Redis.
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\MutableTimeMeter.java`: fake time Bucket4j.
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\RateLimitTestConfiguration.java`: bean test `@Primary`.
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\RateLimitBucketCleaner.java`: limpa maps privados de buckets.
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\modules\users\presentation\controller\WithMockUserId.java`: annotation de security context.
  - `C:\Projetos\stockfy\stockfy-server\src\test\java\br\com\threadstech\stockfy\modules\users\presentation\controller\WithMockUserIdSecurityContextFactory.java`: cria principal UUID e roles.
  - SQL fixtures:
    - `C:\Projetos\stockfy\stockfy-server\src\test\resources\sql\users\cleanup.sql`
    - `C:\Projetos\stockfy\stockfy-server\src\test\resources\sql\users\base-users.sql`
    - `C:\Projetos\stockfy\stockfy-server\src\test\resources\sql\users\endpoint-scenarios.sql`
    - `C:\Projetos\stockfy\stockfy-server\src\test\resources\sql\users\login-scenarios.sql`
    - `C:\Projetos\stockfy\stockfy-server\src\test\resources\sql\users\refresh-token-scenarios.sql`

## 11. Checklist final de cobertura de fluxos internos

- [x] Endpoints HTTP listados com metodo, rota, DTOs, status, authz, validacoes e regras.
- [x] Services/use cases rastreados.
- [x] Handlers globais e do modulo rastreados.
- [x] Eventos e publisher rastreados.
- [x] Listeners/consumers: nao encontrados.
- [x] Filters: `RateLimitFilter`, `JwtAuthenticationFilter`.
- [x] Interceptors: nao encontrados.
- [x] Schedulers/jobs async: nao encontrados.
- [x] Adapters/repositories/mappers rastreados.
- [x] Configs de security, JWT, cookies, HMAC, rate limit, password reset, JPA auditing, locale rastreadas.
- [x] Migrations e tabelas rastreadas.
- [x] Cache/Redis/manual Bucket4j rastreados.
- [x] Mensageria RabbitMQ rastreada.
- [x] Uploads/webhooks/arquivos: nao encontrados.
- [x] Testes e fixtures relacionados listados.
