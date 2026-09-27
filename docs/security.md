# Seguranca, privacidade e DevSecOps

## Controles implementados no codigo

- JWT HMAC-SHA256 com emissor, expiracao e perfis no token;
- BCrypt com fator 12 para senhas;
- autorizacao por `ADMIN`, `MANAGER` e `ADVISOR`;
- validacao de entrada com Jakarta Validation;
- erros de dominio, autenticacao e autorizacao em `application/problem+json`;
- CORS restrito, CSP, bloqueio de frames e API stateless;
- rate limit por origem e headers com limite e saldo;
- correlation ID para rastrear uma requisicao nos logs;
- campanhas com consentimento explicito e minimizacao de dados pessoais.

## Seguranca da infraestrutura

- imagens base reduzidas do Eclipse Temurin;
- processo Java executado por usuario sem privilegios;
- filesystem dos containers em modo somente leitura;
- `no-new-privileges` no Docker Compose;
- bancos e credenciais separados por servico;
- segredos injetados por variaveis de ambiente e `.env` ignorado pelo Git.

Em producao, TLS deve ser encerrado no gateway ou ingress da plataforma. O Compose local nao inclui certificados para manter a demonstracao simples.

## Pipeline DevSecOps

O workflow `.github/workflows/ci.yml` interrompe a entrega quando encontra falhas relevantes:

1. compilacao e testes com Maven;
2. publicacao dos relatorios Surefire como evidencia;
3. Gitleaks para secret scanning;
4. Semgrep para SAST;
5. Trivy para dependencias, configuracoes e filesystem;
6. build das duas imagens;
7. Trivy nas imagens do Identity Service e do Risk Service.

## Modelo de ameacas STRIDE

| Ameaca | Exemplo no Predit | Controle |
| --- | --- | --- |
| Spoofing | uso de identidade falsa | JWT assinado, BCrypt e expiracao curta |
| Tampering | alteracao indevida de score ou campanha | autorizacao por perfil, validacao e migrations versionadas |
| Repudiation | operador nega uma alteracao de lead | logs com ator, evento, recurso e correlation ID |
| Information disclosure | exposicao de dados pessoais ou token | DTOs minimos, logs sanitizados e segredos fora do repositorio |
| Denial of service | rajada contra login ou consultas | rate limit, limites de payload e metricas |
| Elevation of privilege | consultor ativa campanha de gestor | `@PreAuthorize` e testes de acesso negado |

## Observabilidade e resposta

### Sinais monitorados

- disponibilidade e saude via Actuator;
- erros HTTP por status;
- latencia e volume de requisicoes;
- autenticacoes falhas e bloqueios por rate limit;
- criacao e mudanca de status de leads e campanhas;
- metricas Prometheus do Risk Service.

### Fluxo de resposta a incidentes

1. **Deteccao:** alerta por erro, indisponibilidade, latencia ou atividade anormal.
2. **Analise:** correlacao de logs pelo `X-Correlation-ID` e identificacao do escopo.
3. **Contencao:** bloqueio de origem, usuario ou credencial comprometida.
4. **Erradicacao:** correcao da causa e atualizacao de dependencia ou configuracao.
5. **Recuperacao:** restauracao validada e monitoramento reforcado.
6. **Pos-incidente:** registro de causa, impacto e acao preventiva.

## LGPD

O MVP aplica finalidade e minimizacao. Sao persistidos somente dados necessarios para atendimento e retencao. Localizacao precisa, gostos pessoais e telemetria bruta nao sao armazenados. Campanhas contextuais exigem consentimento registrado e revogavel.

Para uma operacao real, o controlador deve definir prazos de retencao, rotina de atendimento aos direitos do titular, base legal por tratamento e processo formal de descarte ou anonimizacao.

## Mapeamento com boas praticas

| Referencia | Aplicacao no projeto |
| --- | --- |
| OWASP API Top 10 | autenticacao, autorizacao por objeto/perfil, limite de consumo, validacao e configuracao segura |
| OWASP ASVS | armazenamento seguro de senha, gestao de sessao stateless, controle de acesso e logging |
| OWASP Mobile Top 10 | API nao confia no aplicativo; toda permissao e validada novamente no servidor |
| LGPD | consentimento, finalidade, minimizacao e separacao de dados de identidade |

## Plano de seguranca continua

- revisar dependencias e alertas do pipeline a cada sprint;
- executar testes de seguranca e autorizacao antes de releases;
- auditar perfis e usuarios ativos mensalmente;
- rotacionar segredos e revogar credenciais quando necessario;
- realizar backup diario dos bancos, teste mensal de restauracao e registro do resultado;
- revisar o modelo de ameacas quando um novo tipo de dado ou integracao for adicionado.
