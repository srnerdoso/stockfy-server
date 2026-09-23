# Especificação Técnica: Mitigação de CSRF e Política de Cookies SameSite

- **Status:** Proposto
- **Data:** 2026-09-23
- **Issue Relacionada:** [#35](https://github.com/srnerdoso/stockfy-server/issues/35)
- **Módulo:** `users` / `config`

## 1. Contexto e Problema

A aplicação Stockfy utiliza autenticação baseada em cookies HTTP (`access_token` e `refresh_token`), gerenciados pelo `AuthController` e validados pelo `JwtAuthenticationFilter`.
Na configuração do Spring Security (`SecurityConfig`), a proteção CSRF padrão encontra-se desabilitada (`csrf(AbstractHttpConfigurer::disable)`).
Simultaneamente, os cookies emitidos pelo `AuthController` possuíam apenas os atributos `HttpOnly` e `Secure`, carecendo da diretiva `SameSite`.

### Modelo de Ameaça (Impacto)
Como os navegadores enviam cookies automaticamente em requisições disparadas contra o domínio de origem, um website malicioso poderia induzir um usuário autenticado a submeter requisições de mutação de estado (`POST`, `PUT`, `PATCH`, `DELETE`) para o backend do Stockfy, aproveitando-se da ausência de validação de origem ou isolamento de contexto no cookie.

## 2. Avaliação das Estratégias de Mitigação

### Opção A: Proteção CSRF Nativa do Spring Security (`CookieCsrfTokenRepository`)
- **Como funciona:** O servidor gera um cookie `XSRF-TOKEN` (com `HttpOnly=false`). O frontend SPA lê o cookie e replica seu valor no cabeçalho `X-XSRF-TOKEN` em cada requisição mutatória.
- **Vantagens:** Proteção em profundidade contra ataques vindos inclusive de subdomínios compartilhados.
- **Desvantagens:** Alto acoplamento e complexidade operacional no frontend; quebra todos os clientes e testes de integração que realizam mutações sem o token CSRF explícito; desnecessário quando o backend e o frontend operam sob o mesmo site/origem.

### Opção B: Mitigação por Diretiva `SameSite=Strict` em Cookies (Estratégia Adotada)
- **Como funciona:** Os cookies `access_token` e `refresh_token` são marcados com `SameSite=Strict`.
- **Efeito prático:** O navegador restringe o envio dos cookies exclusivamente a requisições com origem no mesmo domínio (`Same-Site`). Em qualquer requisição cross-origin (incluindo navegação por links externos, formulários em sites de terceiros ou chamadas `fetch`/`XMLHttpRequest` externas), os cookies de autenticação **não** são anexados pelo navegador.
- **Alinhamento OWASP:** Conforme o *OWASP Cross-Site Request Forgery Prevention Cheat Sheet*, a diretiva `SameSite=Strict` é uma defesa primária extremamente eficaz para APIs REST e SPAs de primeiro domínio (same-origin/same-site).
- **Decisão:** Manter `csrf(AbstractHttpConfigurer::disable)` explicitamente documentado no `SecurityConfig`, aplicando `SameSite=Strict` em todos os cookies emitidos pela aplicação (`access_token`, `refresh_token` e cookies expirados em logout).

## 3. Critérios de Aceite Atendidos

1. Todos os cookies emitidos pelo `AuthController` possuem `SameSite=Strict`, `HttpOnly=true`, `Secure=true` e `Path=/`.
2. A política de cookies é parametrizada em `SecurityProperties` (`stockfy.security.cookies.same-site` e `stockfy.security.cookies.secure`).
3. O `SecurityConfig` possui documentação explícita justificando a desativação do CSRF em virtude da arquitetura stateless e da mitigação via `SameSite=Strict`.
4. Testes automatizados cobrem a presença de `SameSite=Strict` nos fluxos de login, refresh e logout.
