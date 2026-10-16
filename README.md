# ZapFlow

WhatsApp messaging application built with Java 21, Spring Boot 4 and Evolution API v2.
The current integration sends text through an Evolution instance connected by QR code.

## Planned application scope

CSV import, consent-aware contact selection and batch sending remain planned.
Application data stays in memory under ADR 0002. PostgreSQL and Redis in Compose
are dependencies of Evolution, not persistence for Zapflow contacts.

## Architecture

```text
HTTP controller -> input port -> application service -> output port -> Evolution adapter
```

Application models and ports do not depend on Spring or HTTP DTOs. See
[Evolution integration](docs/evolution-whatsapp.md) for configuration and request examples.

## Setup

Copy `.env.example` to `.env` and fill in the values. Never commit credentials.
For local Evolution, configure `AUTHENTICATION_API_KEY` and `POSTGRES_PASSWORD`:

```powershell
docker compose up -d
```

Open `http://localhost:8081/manager`, create/connect an instance and scan its QR code.
Set `WHATSAPP_PROVIDER_INSTANCE` to its exact name and `WHATSAPP_PROVIDER_API_KEY`
to an authorized Evolution key. The provider URL defaults to `http://localhost:8081`.
Hosted Evolution can be used by replacing the provider URL and credentials.

Spring imports `.env` from the working directory. Restart the Java app after changes.

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

On Linux/macOS, use `./mvnw`. Health check: `GET /actuator/health`.

## Send a message

`POST http://localhost:8080/whatsapp/messages`, `Content-Type: application/json`:

```json
{
  "number": "5511999999999",
  "textMessage": {"text": "Hello from Zapflow!"}
}
```

Replace the recipient before sending. Success returns `key.id`, `message.conversation`
and `status`. Acceptance does not confirm delivery. Tests use simulated provider responses.

## Documentation

- [Current Evolution setup, architecture and errors](docs/evolution-whatsapp.md)
- [Contributing](CONTRIBUTING.md)
- Original planning documents in `docs/` describe a Meta Cloud API scope. Direct Meta
  access, WABA template management, campaigns and webhooks are not implemented by
  the current text messaging flow.

## CI

GitHub Actions runs `./mvnw verify` for the configured pushes and pull requests.

## Team

- Diego Lima
- [@yago1290](https://github.com/yago1290)

Private project - all rights reserved.

