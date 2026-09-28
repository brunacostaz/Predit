# Evidencia de testes automatizados

Comando executado em 28/09/2026:

```powershell
.\mvnw.cmd clean verify
```

Resultado: **57 testes executados, 0 falhas e 0 erros**. As tres migrations do Risk Service tambem foram aplicadas e validadas durante o build.

## Como os testes estao organizados

| Classe | O que prova | Criterio da rubrica |
| --- | --- | --- |
| `AuthControllerTest` | login, claims do JWT, validade de 30 min, credenciais invalidas, JSON malformado | JWT, erros |
| `UserControllerTest` | CRUD de usuarios com tokens reais, 401/403/404/409/400 | Autenticacao e autorizacao |
| `RateLimitTest` | bloqueio de forca bruta no login com 429 e `Retry-After` | Seguranca |
| `JwtValidationTest` | `JwtDecoder` real do Risk Service: expirado, adulterado, outra chave, outro emissor, sem perfis | JWT |
| `CustomerApiTest` | recursos aninhados, `Location`, historico de inferencias, 404 para veiculo de outro cliente | REST nivel 2 |
| `LeadApiTest` | fluxo de leads, consentimento (422), perfis | REST nivel 2, perfis |
| `CampaignControllerTest` | criacao, consulta, ativacao, 403/400/404 | REST nivel 2 |
| `CustomerAuthorizationTest` | MANAGER cria, ADVISOR recebe 403 | Perfis |
| `DashboardControllerTest` | 401 anonimo, indicadores para MANAGER | Endpoints protegidos |
| `ErrorHandlingTest` | 400/404/405/415 em `application/problem+json`, sem nomes internos | Padronizacao de erros |
| `HttpErrorResponseTest` | mesmos erros sobre HTTP real (Tomcat + filtros de seguranca) | Padronizacao de erros |
| `FlywayMigrationTest` | schema e dados de demonstracao | Arquitetura |

Os testes do Risk Service que usam `jwt()` do Spring Security Test focam em regras de negocio; a validacao criptografica do token e coberta separadamente por `JwtValidationTest`, que assina tokens reais com a mesma chave e emissor do Identity Service.

## Resultado por teste

| Servico | Classe | Teste | Resultado |
| --- | --- | --- | --- |
| `identity-service` | `AuthControllerTest` | `shouldRejectMalformedJson` | passou |
| `identity-service` | `AuthControllerTest` | `shouldRejectUnknownUserWithSameMessage` | passou |
| `identity-service` | `AuthControllerTest` | `shouldRejectInvalidCredentials` | passou |
| `identity-service` | `AuthControllerTest` | `shouldIssueJwtForValidCredentials` | passou |
| `identity-service` | `AuthControllerTest` | `shouldRejectInvalidPayload` | passou |
| `identity-service` | `RateLimitTest` | `loginIsThrottledAfterLimit` | passou |
| `identity-service` | `UserControllerTest` | `duplicatedEmailReturnsConflict` | passou |
| `identity-service` | `UserControllerTest` | `weakPasswordReturnsValidationError` | passou |
| `identity-service` | `UserControllerTest` | `anonymousCannotListUsers` | passou |
| `identity-service` | `UserControllerTest` | `adminCreatesUserReachableThroughLocation` | passou |
| `identity-service` | `UserControllerTest` | `tamperedTokenIsRejected` | passou |
| `identity-service` | `UserControllerTest` | `unknownUserReturnsNotFound` | passou |
| `identity-service` | `UserControllerTest` | `managerCannotManageUsers` | passou |
| `identity-service` | `UserControllerTest` | `expiredTokenIsRejected` | passou |
| `risk-service` | `FlywayMigrationTest` | `shouldApplyAllDatabaseMigrations` | passou |
| `risk-service` | `CampaignControllerTest` | `createdCampaignIsReachableAndCanBeActivated` | passou |
| `risk-service` | `CampaignControllerTest` | `unknownCampaignReturnsNotFound` | passou |
| `risk-service` | `CampaignControllerTest` | `missingCampaignReturnsNotFound` | passou |
| `risk-service` | `CampaignControllerTest` | `managerCanCreateCampaign` | passou |
| `risk-service` | `CampaignControllerTest` | `advisorCannotCreateCampaign` | passou |
| `risk-service` | `CampaignControllerTest` | `invalidCampaignReturnsProblemDetail` | passou |
| `risk-service` | `CustomerApiTest` | `invalidVinReturnsValidationError` | passou |
| `risk-service` | `CustomerApiTest` | `duplicatedEmailReturnsConflict` | passou |
| `risk-service` | `CustomerApiTest` | `createdCustomerIsReachableThroughLocationHeader` | passou |
| `risk-service` | `CustomerApiTest` | `scoreOutOfRangeIsRejected` | passou |
| `risk-service` | `CustomerApiTest` | `unknownCustomerReturnsNotFound` | passou |
| `risk-service` | `CustomerApiTest` | `modelInferenceIsStoredAndExposedAsResource` | passou |
| `risk-service` | `CustomerApiTest` | `advisorCannotRegisterModelInference` | passou |
| `risk-service` | `CustomerApiTest` | `assessmentForVehicleOfAnotherCustomerIsRejected` | passou |
| `risk-service` | `CustomerAuthorizationTest` | `managerCanCreateCustomer` | passou |
| `risk-service` | `CustomerAuthorizationTest` | `advisorCannotCreateCustomer` | passou |
| `risk-service` | `DashboardControllerTest` | `shouldReturnSummaryForManager` | passou |
| `risk-service` | `DashboardControllerTest` | `shouldRejectAnonymousAccess` | passou |
| `risk-service` | `ErrorHandlingTest` | `invalidUuidReturnsBadRequestWithoutInternalDetails` | passou |
| `risk-service` | `ErrorHandlingTest` | `unsupportedMediaTypeReturns415` | passou |
| `risk-service` | `ErrorHandlingTest` | `malformedJsonReturnsBadRequest` | passou |
| `risk-service` | `ErrorHandlingTest` | `unsupportedMethodReturnsMethodNotAllowed` | passou |
| `risk-service` | `ErrorHandlingTest` | `unknownEndpointReturnsNotFound` | passou |
| `risk-service` | `ErrorHandlingTest` | `invalidEnumFilterDoesNotLeakClassNames` | passou |
| `risk-service` | `HttpErrorResponseTest` | `malformedJsonIsBadRequestOverRealHttp` | passou |
| `risk-service` | `HttpErrorResponseTest` | `unknownEndpointIsNotFoundOverRealHttp` | passou |
| `risk-service` | `HttpErrorResponseTest` | `anonymousRequestIsUnauthorizedOverRealHttp` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsMissingToken` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsTamperedPayload` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsTokenFromUnknownIssuer` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsMalformedToken` | passou |
| `risk-service` | `JwtValidationTest` | `rolesClaimDrivesAuthorization` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsExpiredToken` | passou |
| `risk-service` | `JwtValidationTest` | `tokenWithoutRolesCannotReadProtectedResources` | passou |
| `risk-service` | `JwtValidationTest` | `rejectsTokenSignedWithAnotherSecret` | passou |
| `risk-service` | `JwtValidationTest` | `acceptsValidTokenSignedByIdentityService` | passou |
| `risk-service` | `LeadApiTest` | `unknownLeadReturnsNotFound` | passou |
| `risk-service` | `LeadApiTest` | `advisorCanUpdateLeadStatus` | passou |
| `risk-service` | `LeadApiTest` | `managerCreatesLeadReachableThroughLocation` | passou |
| `risk-service` | `LeadApiTest` | `leadRejectsVehicleOfAnotherCustomer` | passou |
| `risk-service` | `LeadApiTest` | `advisorCannotCreateLead` | passou |
| `risk-service` | `LeadApiTest` | `leadRequiresContactConsent` | passou |

Os relatorios XML e TXT sao gerados pelo Maven Surefire dentro das pastas `target/surefire-reports`. No GitHub, o job **Testes automatizados** repete o mesmo comando a cada push e pull request.
