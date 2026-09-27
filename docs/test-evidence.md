# Evidencia de testes automatizados

Comando executado em 27/09/2026:

```powershell
.\mvnw.cmd clean verify
```

Resultado: **12 testes executados, 0 falhas e 0 erros**. As tres migrations do Risk Service tambem foram aplicadas e validadas durante o build.

## Cenarios cobertos

| Teste | Cenario | Resultado esperado |
| --- | --- | --- |
| `shouldIssueJwtForValidCredentials` | login correto | 200, JWT, perfil e expiracao validos |
| `shouldRejectInvalidCredentials` | senha incorreta | 401 |
| `shouldRejectInvalidPayload` | payload invalido | 400 com `ProblemDetail` |
| `shouldRejectAnonymousAccess` | dashboard sem token | 401 |
| `shouldReturnSummaryForManager` | dashboard com MANAGER | 200 e indicadores |
| `advisorCannotCreateCustomer` | permissao insuficiente | 403 |
| `managerCanCreateCustomer` | cadastro autorizado | 201 |
| `managerCanCreateCampaign` | campanha valida | 201 |
| `advisorCannotCreateCampaign` | perfil sem permissao | 403 |
| `invalidCampaignReturnsProblemDetail` | dados invalidos | 400 com mapa de erros |
| `missingCampaignReturnsNotFound` | recurso inexistente | 404 |
| `shouldApplyAllDatabaseMigrations` | schema e dados iniciais | 3 migrations aplicadas |

Os relatorios XML e TXT sao gerados pelo Maven Surefire dentro das pastas `target/surefire-reports`. No GitHub, o job **Testes automatizados** repete o mesmo comando a cada push e pull request.
