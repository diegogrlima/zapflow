# ZapFlow

Envia uma mensagem de WhatsApp para uma lista de contatos com consentimento: importa um CSV, monta a mensagem e envia para todos. Java 21 + Spring Boot 4, envio pela **Evolution API v2** ([decisão 0003](docs/decisoes/0003-evolution-api.md)), sem banco de dados ([decisão 0002](docs/decisoes/0002-sem-banco-de-dados.md)).

## O que já funciona

| Funcionalidade | Rota | Issue |
|----------------|------|-------|
| Importar contatos de um CSV (`nome`, `telefone`, `opt_in` + colunas extras) | `POST /imports` (multipart, campo `file`) | #6 |
| Enviar um texto para um contato importado | `POST /whatsapp/messages` | #8, #10 |

Ainda falta: montar a mensagem com variáveis do CSV (#9) e enviar para todos com controle de ritmo (#11).

## Como rodar

1. Copie `.env.example` para `.env` e preencha. Nunca faça commit do `.env`.
2. Suba a Evolution: `docker compose up -d`.
3. Abra `http://localhost:8081/manager`, crie uma instância e leia o QR Code com o WhatsApp do número de envio (use um número dedicado, não o pessoal).
4. Coloque o nome da instância em `WHATSAPP_PROVIDER_INSTANCE` e a chave em `WHATSAPP_PROVIDER_API_KEY`.
5. Rode a aplicação:

```bash
./mvnw verify            # build e testes (no Windows: .\mvnw.cmd verify)
./mvnw spring-boot:run   # sobe em http://localhost:8080
```

Health check: `GET /actuator/health`. Detalhes da integração e dos erros em [docs/evolution-whatsapp.md](docs/evolution-whatsapp.md).

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [docs/escopo.md](docs/escopo.md) | O que o projeto faz e o que está fora |
| [docs/arquitetura.md](docs/arquitetura.md) | Arquitetura Hexagonal: pacotes, portas e fluxos |
| [docs/modelo-de-dados.md](docs/modelo-de-dados.md) | Objetos do domínio (em memória) |
| [docs/evolution-whatsapp.md](docs/evolution-whatsapp.md) | Configuração, envio e erros da Evolution |
| [docs/regras-whatsapp.md](docs/regras-whatsapp.md) | Boas práticas para não ter o número bloqueado |
| [docs/decisoes/](docs/decisoes/) | Registro de decisões (ADRs) |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Branches, commits e PRs |

## CI

GitHub Actions roda `./mvnw -B verify` em push e PR para `main` e `develop`.

## Equipe

- Diego Lima ([@diegogrlima](https://github.com/diegogrlima))
- Yago ([@yago1290](https://github.com/yago1290))

Projeto privado. Todos os direitos reservados.
