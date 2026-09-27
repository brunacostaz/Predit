# Predit Backend

**Plataforma de inteligencia para aumentar o VIN Share da Ford por meio de previsao de evasao, priorizacao de clientes e acoes de retencao.**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/Security-JWT-111111?logo=jsonwebtokens&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-12_passing-25A162?logo=junit5&logoColor=white)

Predit e o backend do dashboard gerencial apresentado no Ford Challenge. A solucao consolida informacoes de clientes e veiculos, recebe resultados de modelos preditivos e transforma risco de evasao em uma fila clara de trabalho para a concessionaria.

O objetivo nao e apenas mostrar quem apresenta risco. O sistema explica **por que o cliente esta em risco**, indica **qual acao deve ser tomada** e permite acompanhar o resultado por meio de leads e campanhas.

> Este repositorio corresponde a entrega de Sprint 3 da disciplina de Arquitetura Orientada a Servicos e Web Services da FIAP.

---

## Sumario

- [Contexto do projeto](#contexto-do-projeto)
- [Solucao proposta](#solucao-proposta)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Como a IA participa da solucao](#como-a-ia-participa-da-solucao)
- [Tecnologias](#tecnologias)
- [Modelo de dominio](#modelo-de-dominio)
- [Seguranca e perfis de acesso](#seguranca-e-perfis-de-acesso)
- [Como executar com Docker](#como-executar-com-docker)
- [Como demonstrar o projeto](#como-demonstrar-o-projeto)
- [Endpoints](#endpoints)
- [Exemplos de requisicao](#exemplos-de-requisicao)
- [Erros e status HTTP](#erros-e-status-http)
- [Testes automatizados](#testes-automatizados)
- [Observabilidade](#observabilidade)
- [Pipeline DevSecOps](#pipeline-devsecops)
- [Estrutura do repositorio](#estrutura-do-repositorio)
- [Documentacao complementar](#documentacao-complementar)
- [Solucao de problemas](#solucao-de-problemas)

---

## Contexto do projeto

VIN Share representa a parcela de veiculos Ford que continua utilizando a rede oficial para manutencoes e servicos. Quando um cliente deixa de retornar a concessionaria, a rede perde receita de pecas e mao de obra, reduz a oportunidade de relacionamento e pode comprometer a recompra futura.

O problema operacional e que os sinais de evasao ficam distribuidos entre historico de revisoes, garantia, quilometragem, interacoes e comportamento de atendimento. Sem uma visao consolidada, a concessionaria tende a agir apenas depois que o cliente ja deixou a rede.

Predit organiza esses sinais em uma jornada proativa:

1. um modelo analitico calcula o risco associado ao VIN;
2. a API registra o score e os fatores explicativos;
3. o dashboard destaca clientes e modelos prioritarios;
4. o gestor cria leads ou campanhas de retencao;
5. a equipe acompanha contato, conversao e impacto potencial.

## Solucao proposta

A solucao foi dividida em dois microservicos independentes:

| Servico | Porta | Banco | Responsabilidade |
| --- | ---: | --- | --- |
| `identity-service` | `8081` | `predit_identity` | usuarios, senhas, perfis, login e emissao de JWT |
| `risk-service` | `8082` | `predit_risk` | clientes, veiculos, scores, dashboard, leads e campanhas |

Cada servico possui banco proprio e migrations independentes. O Risk Service nao consulta tabelas do Identity Service: ele valida localmente a assinatura, o emissor, a expiracao e os perfis presentes no JWT.

## Funcionalidades

- autenticacao stateless com JWT de curta duracao;
- controle de acesso por `ADMIN`, `MANAGER` e `ADVISOR`;
- cadastro de clientes e veiculos identificados por VIN;
- historico de avaliacoes preditivas com nome e versao do modelo;
- explicacao dos fatores de risco e recomendacao de proxima acao;
- filtros de clientes por risco, concessionaria, nome, email ou VIN;
- dashboard com VIN Share, veiculos monitorados, risco medio e receita em risco;
- distribuicao de risco e score medio por modelo;
- criacao e acompanhamento de leads proativos;
- planejamento e ativacao de campanhas de retencao;
- validacao de consentimento para acoes de relacionamento;
- documentacao interativa com OpenAPI e Swagger UI;
- respostas de erro padronizadas com `application/problem+json`;
- logs JSON com correlation ID;
- metricas para Prometheus e health checks com Spring Actuator;
- migrations Flyway e dados ficticios para demonstracao;
- execucao completa com Docker Compose;
- pipeline com testes e verificacoes de seguranca.

---

## Arquitetura

```mermaid
flowchart LR
    APP[Aplicativo do cliente]
    DASH[Dashboard Predit]
    MODEL[Modelo de IA]
    ID[Identity Service :8081]
    RISK[Risk Service :8082]
    IDDB[(PostgreSQL Identity)]
    RISKDB[(PostgreSQL Risk)]

    APP -->|login| ID
    DASH -->|login| ID
    ID --> IDDB
    ID -->|JWT assinado| APP
    ID -->|JWT assinado| DASH
    APP -->|Bearer JWT| RISK
    DASH -->|Bearer JWT| RISK
    MODEL -->|score, explicacao e versao| RISK
    RISK --> RISKDB
```

### Fluxo de autenticacao

1. O usuario envia email e senha para `POST /api/v1/auth/login`.
2. O Identity Service localiza o usuario e compara a senha com o hash BCrypt.
3. Um JWT HS256 e emitido com `sub`, `iss`, `iat`, `exp`, `email`, `name` e `roles`.
4. O cliente envia o token no header `Authorization: Bearer <token>`.
5. O Risk Service valida assinatura, emissor e expiracao sem acessar o banco de identidade.
6. Spring Security aplica a regra do endpoint e do perfil autenticado.

### Separacao de responsabilidades

| Camada | Responsabilidade |
| --- | --- |
| `api` | controllers, DTOs, validacao de entrada e contrato HTTP |
| `service` | regras de negocio e coordenacao dos casos de uso |
| `domain` | entidades, enums e repositories do dominio |
| `config` | seguranca, OpenAPI e configuracao da aplicacao |
| `security` | rate limit, correlation ID e apoio ao JWT |
| `error` | excecoes de dominio e respostas RFC 9457 |
| `db/migration` | criacao e evolucao versionada do schema |

O detalhamento dos componentes e diagramas de sequencia esta em [docs/architecture.md](docs/architecture.md).

## Como a IA participa da solucao

O modelo de machine learning fica desacoplado da API. Ele pode ser treinado e executado em Python, notebook ou plataforma de dados, desde que publique uma inferencia no contrato do Risk Service.

Para cada veiculo, o modelo envia:

- `score`: probabilidade operacional de evasao, de 0 a 100;
- `reasons`: fatores que mais influenciaram a previsao;
- `recommendedAction`: acao sugerida para a concessionaria;
- `modelName`: identificacao do modelo;
- `modelVersion`: versao usada para auditoria e comparacao.

O backend calcula a faixa de risco a partir do score, preserva o historico da inferencia e disponibiliza os dados para o dashboard. Dessa forma, um novo modelo pode substituir o anterior sem alterar os demais recursos da plataforma.

Importante: o score representa uma probabilidade para apoiar decisao humana. Ele nao e tratado como certeza nem dispara contato automaticamente.

---

## Tecnologias

| Tecnologia | Uso no projeto |
| --- | --- |
| Java 21 | linguagem principal |
| Spring Boot 3.4.5 | base dos microservicos |
| Spring Web | APIs REST |
| Spring Data JPA / Hibernate | persistencia e repositories |
| Spring Security | autenticacao e autorizacao |
| OAuth2 Resource Server | validacao de Bearer JWT |
| JJWT | emissao de tokens no Identity Service |
| Jakarta Validation | validacao de payloads |
| PostgreSQL 17 | bancos dos servicos em Docker |
| Flyway | versionamento do schema e seed de demonstracao |
| H2 | testes e perfil local alternativo |
| Springdoc OpenAPI | Swagger UI e contrato OpenAPI |
| Spring Actuator | health, metricas e informacoes operacionais |
| Micrometer Prometheus | exposicao de metricas do Risk Service |
| Logstash Encoder | logs estruturados em JSON |
| JUnit 5 / MockMvc | testes automatizados |
| Maven | build multi-modulo e dependencias |
| Docker / Compose | empacotamento e orquestracao local |
| GitHub Actions | integracao continua e DevSecOps |

## Modelo de dominio

```mermaid
erDiagram
    CUSTOMER ||--|| VEHICLE : owns
    VEHICLE ||--o{ RISK_ASSESSMENT : receives
    CUSTOMER ||--o{ LEAD : generates
    VEHICLE ||--o{ LEAD : motivates

    CUSTOMER {
        uuid id
        string name
        string email
        string dealership
        boolean contact_consent
    }
    VEHICLE {
        uuid id
        string vin
        string model
        int model_year
        int mileage
    }
    RISK_ASSESSMENT {
        uuid id
        int score
        string level
        string model_name
        string model_version
    }
    LEAD {
        uuid id
        string status
        string priority
        string assigned_to
    }
    CAMPAIGN {
        uuid id
        string segment
        string status
        decimal estimated_revenue
    }
```

As campanhas representam estrategias por segmento e nao dependem de um unico cliente. Os dados de demonstracao incluem Ranger, Bronco, Maverick, Mustang e Territory em diferentes faixas de risco.

---

## Seguranca e perfis de acesso

### Perfis

| Operacao | ADMIN | MANAGER | ADVISOR |
| --- | :---: | :---: | :---: |
| consultar dashboard, clientes, leads e campanhas | sim | sim | sim |
| criar usuarios | sim | nao | nao |
| cadastrar cliente e veiculo | sim | sim | nao |
| registrar inferencia do modelo | sim | sim | nao |
| criar lead | sim | sim | nao |
| atualizar status de lead | sim | sim | sim |
| criar ou ativar campanha | sim | sim | nao |

### Controles implementados

- senhas com BCrypt fator 12;
- JWT HS256 com emissor e expiracao de 30 minutos;
- API stateless, sem sessao de servidor;
- autorizacao por endpoint e por metodo com `@PreAuthorize`;
- CORS com origens permitidas por configuracao;
- CSP, bloqueio de frames e headers seguros;
- rate limit por origem;
- validacao de tamanho, formato, email, VIN e limites numericos;
- mensagens de autenticacao sem expor detalhes internos;
- segredos e senhas recebidos por variaveis de ambiente;
- containers executados por usuario sem privilegios;
- filesystem dos containers em modo somente leitura;
- logs sem senha ou token;
- consentimento registrado para campanhas de relacionamento.

A analise STRIDE, o mapeamento OWASP/LGPD e o plano de resposta a incidentes estao em [docs/security.md](docs/security.md).

---

## Como executar com Docker

### 1. Pre-requisitos

- Git;
- Docker Desktop com Docker Compose;
- Java 21 apenas para compilar e executar os testes localmente.

Confirme as instalacoes:

```powershell
java -version
docker version
docker compose version
```

### 2. Clonar o repositorio

```powershell
git clone https://github.com/brunacostaz/Predit.git
cd Predit
```

### 3. Configurar variaveis

Crie o arquivo `.env` a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Variaveis disponiveis:

| Variavel | Finalidade | Valor local de exemplo |
| --- | --- | --- |
| `JWT_SECRET` | chave compartilhada para assinar e validar JWT | minimo de 32 caracteres |
| `ADMIN_EMAIL` | email do administrador inicial | `admin@predit.com.br` |
| `ADMIN_PASSWORD` | senha inicial do administrador | `ChangeMe123!` |
| `IDENTITY_DB_PASSWORD` | senha do banco de identidade | `identity_dev_password` |
| `RISK_DB_PASSWORD` | senha do banco de risco | `risk_dev_password` |
| `VIN_SHARE_PERCENT` | indicador de referencia do dashboard | `68.0` |

Os valores padrao existem somente para demonstracao local. Troque todos os segredos antes de qualquer ambiente compartilhado.

### 4. Compilar os servicos

```powershell
.\mvnw.cmd clean package
```

Esse comando tambem executa os testes. Os JARs sao gerados em:

```text
identity-service/target/identity-service-1.0.0.jar
risk-service/target/risk-service-1.0.0.jar
```

### 5. Subir o ambiente

```powershell
docker compose up --build -d
```

O Compose cria quatro containers:

- PostgreSQL do Identity Service;
- PostgreSQL do Risk Service;
- Identity Service;
- Risk Service.

Confira o estado:

```powershell
docker compose ps
docker compose logs -f identity-service risk-service
```

### 6. Acessar os recursos

| Recurso | URL |
| --- | --- |
| Identity Swagger | `http://localhost:8081/swagger-ui.html` |
| Identity OpenAPI | `http://localhost:8081/v3/api-docs` |
| Identity Health | `http://localhost:8081/actuator/health` |
| Risk Swagger | `http://localhost:8082/swagger-ui.html` |
| Risk OpenAPI | `http://localhost:8082/v3/api-docs` |
| Risk Health | `http://localhost:8082/actuator/health` |
| Risk Prometheus | `http://localhost:8082/actuator/prometheus` |

### 7. Encerrar o ambiente

```powershell
docker compose down
```

Para tambem apagar os volumes e reiniciar os dados ficticios:

```powershell
docker compose down -v
```

### Alternativa sem Docker

O perfil `demo` usa H2 em memoria. Abra dois terminais na raiz:

```powershell
.\mvnw.cmd -pl identity-service spring-boot:run "-Dspring-boot.run.profiles=demo"
```

```powershell
.\mvnw.cmd -pl risk-service spring-boot:run "-Dspring-boot.run.profiles=demo"
```

---

## Como demonstrar o projeto

### Credencial inicial

```text
Email: admin@predit.com.br
Senha: ChangeMe123!
Perfil: ADMIN
```

### Fluxo recomendado no Swagger

1. Abra o Swagger do Identity Service.
2. Execute `POST /api/v1/auth/login` com a credencial inicial.
3. Copie o campo `accessToken` da resposta.
4. Abra o Swagger do Risk Service e clique em **Authorize**.
5. Informe `Bearer <accessToken>`.
6. Execute `GET /api/v1/dashboard/summary` para mostrar os indicadores.
7. Execute `GET /api/v1/customers?riskLevel=CRITICAL` para localizar clientes prioritarios.
8. Abra um cliente e mostre score, explicacao, acao e versao do modelo.
9. Consulte `GET /api/v1/leads` e atualize um lead com `PATCH`.
10. Consulte `GET /api/v1/campaigns` e ative uma campanha.

O arquivo [http/predit-api.http](http/predit-api.http) contem o mesmo roteiro pronto para IntelliJ IDEA ou VS Code com REST Client.

---

## Endpoints

### Identity Service

| Metodo | Endpoint | Acesso | Status de sucesso | Finalidade |
| --- | --- | --- | ---: | --- |
| `POST` | `/api/v1/auth/login` | publico | `200` | autenticar e emitir JWT |
| `GET` | `/api/v1/users` | ADMIN | `200` | listar usuarios internos |
| `POST` | `/api/v1/users` | ADMIN | `201` | criar usuario e definir perfil |

### Risk Service

| Metodo | Endpoint | Acesso | Status de sucesso | Finalidade |
| --- | --- | --- | ---: | --- |
| `GET` | `/api/v1/dashboard/summary` | todos | `200` | consolidar indicadores do dashboard |
| `GET` | `/api/v1/customers` | todos | `200` | listar e filtrar clientes |
| `GET` | `/api/v1/customers/{id}` | todos | `200` | detalhar cliente, veiculo e risco atual |
| `POST` | `/api/v1/customers` | ADMIN, MANAGER | `201` | cadastrar cliente e veiculo |
| `POST` | `/api/v1/customers/{customerId}/vehicles/{vehicleId}/risk-assessments` | ADMIN, MANAGER | `201` | registrar inferencia do modelo |
| `GET` | `/api/v1/leads` | todos | `200` | listar oportunidades priorizadas |
| `POST` | `/api/v1/leads` | ADMIN, MANAGER | `201` | criar lead proativo |
| `PATCH` | `/api/v1/leads/{id}/status` | todos | `200` | atualizar andamento e responsavel |
| `GET` | `/api/v1/campaigns` | todos | `200` | listar campanhas de retencao |
| `POST` | `/api/v1/campaigns` | ADMIN, MANAGER | `201` | planejar campanha |
| `PATCH` | `/api/v1/campaigns/{id}/status` | ADMIN, MANAGER | `200` | alterar estado da campanha |

Filtros de clientes:

```text
GET /api/v1/customers?riskLevel=CRITICAL
GET /api/v1/customers?dealership=Ford%20Lapa
GET /api/v1/customers?query=Ranger
```

---

## Exemplos de requisicao

### Login

```json
{
  "email": "admin@predit.com.br",
  "password": "ChangeMe123!"
}
```

Resposta resumida:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresAt": "2026-09-27T20:00:00Z",
  "user": {
    "name": "Predit Administrator",
    "email": "admin@predit.com.br",
    "role": "ADMIN"
  }
}
```

### Cadastro de cliente e veiculo

```json
{
  "name": "Juliana Martins",
  "email": "juliana@example.com",
  "phone": "+5511999999999",
  "dealership": "Ford Lapa",
  "contactConsent": true,
  "vin": "1FTER4FH0RLE12345",
  "model": "Ranger",
  "modelYear": 2025,
  "mileage": 18400,
  "warrantyEndDate": "2028-03-15",
  "lastServiceDate": "2026-01-10"
}
```

### Registro de inferencia

```json
{
  "score": 82,
  "reasons": "Revisao vencida, baixa recorrencia e garantia proxima do fim",
  "recommendedAction": "Priorizar contato e reservar uma janela de revisao",
  "modelName": "predit-retention-model",
  "modelVersion": "1.0.0"
}
```

Faixas geradas pelo backend:

| Score | Nivel |
| ---: | --- |
| `0-39` | `LOW` |
| `40-59` | `MEDIUM` |
| `60-79` | `HIGH` |
| `80-100` | `CRITICAL` |

### Criacao de lead

```json
{
  "customerId": "11111111-1111-1111-1111-111111111111",
  "vehicleId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1",
  "title": "Garantia em risco",
  "action": "Explicar o impacto da revisao atrasada e reservar horario",
  "priority": "CRITICAL",
  "dueAt": "2026-09-29T12:00:00Z"
}
```

### Atualizacao de lead

```json
{
  "status": "CONTACTED",
  "assignedTo": "consultor@predit.com.br"
}
```

### Criacao de campanha

```json
{
  "name": "Revisao antes da viagem",
  "description": "Contato preventivo para clientes com revisao proxima",
  "segment": "Ranger com score acima de 60",
  "consentRequired": true,
  "eligibleCustomers": 42,
  "estimatedConversionRate": 18.5,
  "estimatedRevenue": 99960.00
}
```

---

## Erros e status HTTP

A API usa os metodos e status do nivel 2 de maturidade REST:

| Status | Quando ocorre |
| ---: | --- |
| `200 OK` | consulta ou atualizacao concluida |
| `201 Created` | recurso criado com header `Location` |
| `400 Bad Request` | payload invalido ou regra de formato violada |
| `401 Unauthorized` | token ausente, invalido ou expirado |
| `403 Forbidden` | perfil autenticado sem permissao |
| `404 Not Found` | recurso inexistente |
| `409 Conflict` | email, VIN ou regra unica em conflito |
| `429 Too Many Requests` | limite de requisicoes excedido |

Exemplo `application/problem+json`:

```json
{
  "type": "https://predit.com.br/problems/400",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "errors": {
    "name": "must not be blank"
  }
}
```

---

## Testes automatizados

Execute toda a verificacao:

```powershell
.\mvnw.cmd clean verify
```

Cobertura funcional atual:

- login correto e emissao de JWT;
- validacao de claims, perfil e expiracao;
- credenciais invalidas;
- payload invalido;
- acesso anonimo ao dashboard;
- consulta autenticada dos indicadores;
- permissao de MANAGER para criar cliente;
- bloqueio de ADVISOR na mesma operacao;
- criacao autorizada de campanha;
- bloqueio de campanha por perfil;
- campanha invalida e recurso inexistente;
- aplicacao integral das migrations Flyway.

Resultado validado em 27/09/2026: **12 testes, 0 falhas e 0 erros**.

Relatorios:

```text
identity-service/target/surefire-reports
risk-service/target/surefire-reports
```

Veja a matriz completa em [docs/test-evidence.md](docs/test-evidence.md).

## Observabilidade

Os servicos geram logs JSON adequados para coleta por ferramentas como Grafana Loki, Elastic Stack ou Azure Monitor. O Risk Service aceita ou cria o header `X-Correlation-ID`, devolve o identificador na resposta e o inclui nos logs.

Sinais disponiveis:

- disponibilidade via `/actuator/health`;
- metricas JVM e HTTP via Actuator;
- endpoint Prometheus no Risk Service;
- falhas de autenticacao e autorizacao;
- eventos de criacao e alteracao de leads e campanhas;
- headers de limite e saldo de requisicoes.

## Pipeline DevSecOps

O workflow `.github/workflows/ci.yml` executa em push para `main`, `develop` e pull requests:

1. build e testes com Java 21;
2. publicacao dos relatorios Surefire;
3. Gitleaks para secret scanning;
4. Semgrep para SAST;
5. Trivy para dependencias e configuracoes;
6. build das duas imagens Docker;
7. scan de vulnerabilidades criticas nas imagens.

O pipeline falha quando encontra erro de teste ou vulnerabilidade acima do limite configurado.

---

## Estrutura do repositorio

```text
Predit/
|-- .github/workflows/ci.yml
|-- docs/
|   |-- architecture.md
|   |-- rubric.md
|   |-- security.md
|   `-- test-evidence.md
|-- http/predit-api.http
|-- identity-service/
|   |-- src/main/java/br/com/fiap/predit/identity/
|   |-- src/main/resources/db/migration/
|   |-- src/test/
|   |-- Dockerfile
|   `-- pom.xml
|-- risk-service/
|   |-- src/main/java/br/com/fiap/predit/risk/
|   |-- src/main/resources/db/migration/
|   |-- src/test/
|   |-- Dockerfile
|   `-- pom.xml
|-- .env.example
|-- compose.yaml
|-- pom.xml
`-- README.md
```

## Documentacao complementar

| Documento | Conteudo |
| --- | --- |
| [Arquitetura](docs/architecture.md) | componentes, responsabilidades e fluxos de autenticacao e predicao |
| [Seguranca](docs/security.md) | controles, STRIDE, LGPD, observabilidade e resposta a incidentes |
| [Mapa da rubrica](docs/rubric.md) | requisito da Sprint 3, evidencia no codigo e forma de demonstrar |
| [Evidencia de testes](docs/test-evidence.md) | cenarios automatizados e resultado da ultima execucao |
| [Colecao HTTP](http/predit-api.http) | requisicoes prontas para demonstracao |

## Solucao de problemas

### `docker` nao e reconhecido

Abra o Docker Desktop e reinicie o terminal para atualizar o `PATH`. Confirme com `docker version`.

### Porta 8081 ou 8082 ocupada

Identifique o processo:

```powershell
Get-NetTCPConnection -LocalPort 8081,8082 -ErrorAction SilentlyContinue
```

Encerre o processo responsavel ou altere o mapeamento de portas no `compose.yaml`.

### Banco nao fica saudavel

Consulte os logs:

```powershell
docker compose logs identity-db risk-db
```

Para recriar os bancos locais:

```powershell
docker compose down -v
docker compose up --build -d
```

### Token retorna 401

Confirme que:

- o header usa `Bearer` antes do token;
- o token nao expirou;
- os dois servicos usam o mesmo `JWT_SECRET`;
- o issuer e `predit-identity`.

### Endpoint retorna 403

O token e valido, mas o perfil nao possui permissao. Consulte a [matriz de acesso](#perfis).

---

Todos os nomes, VINs, scores, estimativas e valores incluidos nas migrations sao ficticios e destinados exclusivamente a demonstracao academica. Predit e um prototipo e nao representa uma integracao oficial com sistemas internos da Ford.
