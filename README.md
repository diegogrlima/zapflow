# ZapFlow

Bot de comunicação para WhatsApp construído **exclusivamente sobre a API oficial da Meta (WhatsApp Business Platform – Cloud API)**.
O objetivo é importar uma lista de contatos (CSV), montar uma mensagem a partir de um template aprovado e enviá-la para todos os contatos, **respeitando consentimento, políticas e limites da plataforma**, para reduzir o risco de bloqueio da conta.

> Status: **fase de fundação**. Stack: **Java 21 + Spring Boot 4 (Maven)**, Arquitetura Hexagonal, **sem banco de dados** ([0002](docs/decisoes/0002-sem-banco-de-dados.md)).

## Funcionalidades previstas

- **Importação de contatos (CSV)** — telefone normalizado em E.164, deduplicação, coluna `opt_in` obrigatória e relatório de linhas rejeitadas.
- **Montagem da mensagem** — escolha de um template aprovado da conta, variáveis com valor fixo ou coluna do CSV e pré-visualização.
- **Envio para todos** — em segundo plano, com controle de ritmo, retentativa para erros temporários e relatório final por contato.

Tudo fica em memória durante a execução; reiniciar a aplicação apaga os contatos importados e o progresso do envio.

## APIs de integração

| API | Uso no projeto |
|-----|----------------|
| WhatsApp Cloud API (Messages) | Envio de templates com variáveis |
| Message Templates API | Listagem dos templates aprovados |

**Não** são utilizadas bibliotecas não oficiais que automatizam o WhatsApp Web — elas violam os termos de uso e levam a banimento.

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [docs/escopo.md](docs/escopo.md) | Escopo funcional, módulos e requisitos |
| [docs/arquitetura.md](docs/arquitetura.md) | Arquitetura hexagonal, pacotes e fluxos |
| [docs/regras-whatsapp.md](docs/regras-whatsapp.md) | Regras, limites, custos e políticas da plataforma |
| [docs/modelo-de-dados.md](docs/modelo-de-dados.md) | Objetos do domínio (em memória) |
| [docs/roadmap.md](docs/roadmap.md) | Fases de entrega |
| [docs/setup-meta.md](docs/setup-meta.md) | Passo a passo para configurar a conta na Meta |
| [docs/decisoes/](docs/decisoes/) | Registros de decisões técnicas (ex.: [0001 — stack](docs/decisoes/0001-stack.md)) |

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
