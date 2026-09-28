# Mapa da rubrica - Sprint 3 SOA

Este documento indica onde cada item pedido na entrega pode ser demonstrado.

| Criterio | Evidencia no projeto | Como demonstrar |
| --- | --- | --- |
| Arquitetura da solucao | `docs/architecture.md`, `compose.yaml` | Mostrar o diagrama, os dois servicos e os dois bancos. |
| Separacao de responsabilidades | pacotes `identity-service` e `risk-service` | Explicar que identidade nao acessa dados de risco e vice-versa. |
| Fluxo de comunicacao e autenticacao | diagramas de sequencia e `SecurityConfig` | Fazer login e usar o mesmo JWT no Risk Service. |
| Autenticacao e controle de acesso | `AuthController`, `UserController`, `@PreAuthorize` | Comparar uma chamada como MANAGER e outra como ADVISOR. |
| Endpoints publicos e protegidos | login/health/Swagger publicos; `/api/v1/**` protegido | Chamar dashboard sem token e observar 401 (`DashboardControllerTest`, `HttpErrorResponseTest`). |
| Perfis diferentes | `Role`, regras em controllers e testes | Mostrar ADMIN, MANAGER e ADVISOR. |
| Geracao e validacao de JWT | `JwtService` e `JwtDecoder` | `AuthControllerTest` (claims e 30 min) e `JwtValidationTest` (token expirado, adulterado, outra chave, outro emissor). |
| Protecao por token | `SecurityConfig` dos dois servicos | Autorizar o Swagger com Bearer token. |
| Expiracao e informacoes no token | `JwtService.issue` | Mostrar `exp`, `email`, `name` e `roles`. |
| REST nivel 2 | controllers de clientes, leads e campanhas | Recursos aninhados, todo `Location` de 201 aponta para um GET existente; GET, POST e PATCH com 200, 201, 400, 401, 403, 404, 405, 409, 415 e 422. |
| Testes automatizados | `src/test/java` dos dois servicos | Executar `.\mvnw.cmd clean verify`. |
| Sucesso, erro e acesso nao autorizado | 12 classes de teste, 57 cenarios (ver `docs/test-evidence.md`) | Abrir o relatorio Surefire ou o job `test` do GitHub Actions. |
| Swagger/OpenAPI | Springdoc nos dois servicos | Abrir `/swagger-ui.html`. |
| Tratamento padronizado de erros | `ApiExceptionHandler` nos dois servicos | Enviar JSON malformado ou metodo invalido e mostrar `ProblemDetail` com status correto (`ErrorHandlingTest`). |
| README de execucao | `README.md` | Seguir o fluxo rapido de demonstracao. |

## Checklist antes da entrega

- [x] Aplicacoes compilam com Java 21.
- [x] Testes executam sem PostgreSQL externo.
- [x] Dockerfiles usam usuario nao-root.
- [x] Segredos reais nao estao versionados.
- [x] Dados de demonstracao sao ficticios.
- [x] Swagger documenta os contratos REST.
- [x] Logs nao registram senha ou token.
- [x] Pipeline inclui testes e verificacoes de seguranca.
