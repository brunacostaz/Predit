# Mapa da rubrica - Sprint 3 SOA

Este documento indica onde cada item pedido na entrega pode ser demonstrado.

| Criterio | Evidencia no projeto | Como demonstrar |
| --- | --- | --- |
| Arquitetura da solucao | `docs/architecture.md`, `compose.yaml` | Mostrar o diagrama, os dois servicos e os dois bancos. |
| Separacao de responsabilidades | pacotes `identity-service` e `risk-service` | Explicar que identidade nao acessa dados de risco e vice-versa. |
| Fluxo de comunicacao e autenticacao | diagramas de sequencia e `SecurityConfig` | Fazer login e usar o mesmo JWT no Risk Service. |
| Autenticacao e controle de acesso | `AuthController`, `UserController`, `@PreAuthorize` | Comparar uma chamada como MANAGER e outra como ADVISOR. |
| Endpoints publicos e protegidos | login/health/Swagger publicos; `/api/v1/**` protegido | Chamar dashboard sem token e observar 401. |
| Perfis diferentes | `Role`, regras em controllers e testes | Mostrar ADMIN, MANAGER e ADVISOR. |
| Geracao e validacao de JWT | `JwtService` e `JwtDecoder` | Abrir o teste que valida claims e expiracao. |
| Protecao por token | `SecurityConfig` dos dois servicos | Autorizar o Swagger com Bearer token. |
| Expiracao e informacoes no token | `JwtService.issue` | Mostrar `exp`, `email`, `name` e `roles`. |
| REST nivel 2 | controllers de clientes, leads e campanhas | Mostrar GET, POST e PATCH com 200, 201, 400, 401, 403 e 404. |
| Testes automatizados | `src/test/java` dos dois servicos | Executar `.\mvnw.cmd clean verify`. |
| Sucesso, erro e acesso nao autorizado | `AuthControllerTest`, `CampaignControllerTest`, `DashboardControllerTest` | Abrir o relatorio Surefire ou o job `test` do GitHub Actions. |
| Swagger/OpenAPI | Springdoc nos dois servicos | Abrir `/swagger-ui.html`. |
| Tratamento padronizado de erros | `ApiExceptionHandler` | Enviar campanha invalida e mostrar `ProblemDetail`. |
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
