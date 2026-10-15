# ZapFlow

Bot de comunicação para WhatsApp construído **exclusivamente sobre a API oficial da Meta (WhatsApp Business Platform – Cloud API)**.
O objetivo é selecionar contatos, padronizar mensagens a partir de templates aprovados e automatizar envios **respeitando consentimento, políticas e limites da plataforma**, para reduzir o risco de bloqueio da conta.

> Status: **fase de fundação**. Stack: **Java 21 + Spring Boot 4 (Maven)**. O repositório contém o esqueleto da aplicação, a pipeline de CI, o escopo, a arquitetura lógica e as regras de negócio.

## Funcionalidades previstas

- **Gestão de contatos** — cadastro, importação, tags e segmentos, seleção manual ou por filtro.
- **Consentimento (opt-in / opt-out)** — registro auditável de cada opt-in por categoria e saída automática por palavra-chave ou botão.
- **Templates** — cadastro espelhado da Meta, submissão para aprovação, sincronização de status e mapeamento de variáveis.
- **Campanhas e envio automatizado** — fila com controle de ritmo, pré-validação (opt-in, template aprovado, limite diário) e retentativas.
- **Webhooks** — status de entrega/leitura, respostas dos usuários, qualidade e status dos templates.
- **Automação conversacional** — respostas automáticas, menus com botões e encaminhamento para humano.
- **Monitoramento** — entregas, falhas, opt-outs, nota de qualidade, uso do limite e custo estimado.

## APIs de integração

| API | Uso no projeto |
|-----|----------------|
| WhatsApp Cloud API (Messages) | Envio de templates, mensagens livres, mídia e botões |
| Message Templates API | Criação, listagem e status dos templates |
| Webhooks | Mensagens recebidas, status, qualidade e templates |
| Phone Number API | Consulta de limite de envio e nota de qualidade |
| Marketing Messages API *(opcional)* | Campanhas promocionais |

**Não** são utilizadas bibliotecas não oficiais que automatizam o WhatsApp Web — elas violam os termos de uso e levam a banimento.

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [docs/escopo.md](docs/escopo.md) | Escopo funcional, módulos e requisitos |
| [docs/arquitetura.md](docs/arquitetura.md) | Arquitetura lógica e fluxos |
| [docs/regras-whatsapp.md](docs/regras-whatsapp.md) | Regras, limites, custos e políticas da plataforma |
| [docs/modelo-de-dados.md](docs/modelo-de-dados.md) | Entidades e relacionamentos (conceitual) |
| [docs/roadmap.md](docs/roadmap.md) | Fases de entrega (MVP → Fase 3) |
| [docs/setup-meta.md](docs/setup-meta.md) | Passo a passo para configurar a conta na Meta |

## Configuração

As credenciais da Meta **nunca** são versionadas. Copie `.env.example` para `.env` e preencha os valores (veja [docs/setup-meta.md](docs/setup-meta.md)).

## Como rodar

Pré-requisito: JDK 21. O Maven vem embutido no projeto (`mvnw`).

```bash
./mvnw verify          # compila e roda os testes
./mvnw spring-boot:run # sobe a aplicação em http://localhost:8080
```

Health check: `GET /actuator/health`.

## CI

O GitHub Actions ([.github/workflows/ci.yml](.github/workflows/ci.yml)) roda `./mvnw verify` (build + testes) em todo push para `main`/`develop` e em todo Pull Request para essas branches.

## Contribuição

Veja [CONTRIBUTING.md](CONTRIBUTING.md) para o fluxo de branches, padrão de commits e abertura de PRs.

## Equipe

- Diego Lima
- [@yago1290](https://github.com/yago1290)

---

Projeto privado — todos os direitos reservados.
