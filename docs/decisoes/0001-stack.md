# 0001 — Stack tecnológica

- **Status:** proposta (aguardando validação de @yago1290)
- **Data:** 2026-10-15
- **Issue:** #1
- **Responsável:** @diegogrlima

## Contexto

O ZapFlow integra com a WhatsApp Cloud API da Meta para enviar templates a contatos com consentimento e processar webhooks (ver [escopo](../escopo.md) e [arquitetura](../arquitetura.md)). A stack precisa atender a requisitos que a arquitetura lógica já fixa:

- **Envio assíncrono via fila**, com controle de ritmo, limite diário e retentativa com backoff (erro `130429` e afins).
- **Webhook rápido**: responder 200 imediatamente, validar `X-Hub-Signature-256` e processar em segundo plano.
- **Idempotência**: chave única por envio e deduplicação de eventos pelo ID da Meta.
- **Dados relacionais e auditáveis**: contatos, consentimentos (nunca apagados), templates, campanhas, mensagens e eventos ([modelo de dados](../modelo-de-dados.md)).
- **Segredos só no ambiente** e logs estruturados correlacionados por `wamid`.

A issue #1 pede a escolha de linguagem, framework, banco, mecanismo de fila e hospedagem.

## Opções comparadas

| Critério | **Java 21 + Spring Boot** | Node.js (TypeScript) + NestJS |
|----------|---------------------------|-------------------------------|
| Familiaridade da equipe | Alta | Média |
| Tipagem e refatoração | Forte, verificada em compilação | Boa, mas apagada em runtime |
| Jobs agendados, retentativa, validação | Nativos no ecossistema Spring (`@Scheduled`, Spring Retry, Bean Validation) | Bibliotecas separadas (BullMQ, class-validator) |
| Banco e migrations | Spring Data JPA/JDBC + Flyway, maduros | TypeORM/Prisma, bons mas mais voláteis |
| Concorrência no worker de envio | Virtual threads (Java 21) para I/O com a Meta | Event loop, simples para I/O |
| Observabilidade | Actuator + Micrometer prontos | Montada à parte |
| Custo de memória/início | Maior | Menor |

## Decisão

| Camada | Escolha | Situação |
|--------|---------|----------|
| Linguagem | **Java 21** (LTS) | Implementado no PR #22 |
| Framework | **Spring Boot 4.1** (web MVC, validation, actuator), pacote `com.zapflow` | Implementado no PR #22 |
| Build | **Maven** com wrapper (`./mvnw`), sem instalação local | Implementado no PR #22 |
| CI | **GitHub Actions** rodando `./mvnw -B verify` em push e PR para `main`/`develop` | Implementado no PR #22 |
| Configuração | Variáveis de ambiente (`.env.example`) mapeadas em `application.yml` sob `zapflow.*` | Implementado no PR #22 |
| Banco | ~~PostgreSQL com Flyway~~ | Substituída por [0002](0002-sem-banco-de-dados.md): sem banco |
| Fila de envio | ~~Tabela no PostgreSQL com `SKIP LOCKED`~~ | Substituída por [0002](0002-sem-banco-de-dados.md): fila em memória |
| Hospedagem | Em aberto: decidir quando houver ambiente de homologação com o número de teste da Meta | Pendente |

### Por que fila no PostgreSQL e não um broker (RabbitMQ, SQS, Redis)

> Superado pelo [0002](0002-sem-banco-de-dados.md). Mantido como histórico.

O volume do MVP é limitado pelo próprio limite diário da Meta (centenas a poucos milhares de envios por dia). Uma tabela de fila no mesmo banco permite gravar a mensagem e enfileirá-la **na mesma transação**, o que dá idempotência e evita mensagem perdida sem precisar de outbox. Pausar ou cancelar campanha vira um `UPDATE`. Se o volume crescer, a interface do worker permite trocar por um broker sem mudar as regras de envio.

## Consequências

**Positivas**
- Um único artefato (jar) e um único serviço de infraestrutura (PostgreSQL) para operar no MVP.
- Validação de webhook, retentativa, agendamento e health check usam recursos nativos do Spring.
- CI já protege `develop` e `main` com build e testes a cada PR.

**Negativas / riscos**
- Consumo de memória maior que uma stack Node, o que pesa em hospedagem barata.
- Fila em tabela exige cuidado com índices e limpeza de mensagens antigas.
- Spring Boot 4 é recente; algumas bibliotecas de terceiros podem demorar a suportá-lo.

## Próximos passos

1. Validar esta decisão com @yago1290 (revisão deste PR).
2. ~~Adicionar PostgreSQL, Spring Data e Flyway ao `pom.xml` na issue #4.~~ Cancelado pelo [0002](0002-sem-banco-de-dados.md).
3. Registrar a escolha de hospedagem em um novo registro (`0003-…`) quando for decidida.
