# Evolution WhatsApp integration

The application uses Evolution API v2 with a WhatsApp instance connected through QR code.
There is no direct Meta API adapter or WABA template endpoint in this implementation.

## Architecture

```text
adapter.in.web.WhatsappController
  -> application.port.in.SendMessageUseCase
  -> application.service.SendMessageService
  -> application.port.out.SendMessagePort
  -> adapter.out.whatsapp.EvolutionWhatsappAdapter
  -> POST /message/sendText/{instance}
```

Application models, ports and services have no Spring, HTTP or adapter imports.
The web adapter maps request/response DTOs; the Evolution adapter maps the external
JSON into `SentMessage`. Configuration wires the implementations using constructor injection.

## Configuration

Spring imports the `.env` in its working directory. Existing environment variables
can also supply these values. Restart the Java application after changing them.

```dotenv
WHATSAPP_PROVIDER_URL=http://localhost:8081
WHATSAPP_PROVIDER_INSTANCE=ZapFlow
WHATSAPP_PROVIDER_API_KEY=your-evolution-key
```

The instance name is case sensitive. The key is sent in the `apikey` header.
For local Docker, also set `AUTHENTICATION_API_KEY` and `POSTGRES_PASSWORD` as
shown in `.env.example`; the provider key must match an authorized Evolution key.
For a hosted Evolution server, use its URL, instance name and API key instead.

## Send text

`POST http://localhost:8080/whatsapp/messages`, `Content-Type: application/json`:

```json
{
  "number": "5511999999999",
  "textMessage": {"text": "Hello from Zapflow!"},
  "delay": 1000,
  "linkPreview": false,
  "mentioned": []
}
```

Replace the example recipient before sending. The adapter converts `textMessage.text`
to the top-level `text` field required by Evolution. Delay, quoted message, link
preview and mentions are optional. Empty quoted objects and mentions are omitted.

Example response:

```json
{
  "key": {"id": "MESSAGE_ID", "remoteJid": "5511999999999@s.whatsapp.net", "fromMe": true},
  "message": {"conversation": "Hello from Zapflow!"},
  "status": "PENDING"
}
```

The returned message ID belongs to Evolution. `PENDING` indicates acceptance, not
confirmed delivery. This endpoint sends plain text and does not list WABA templates.

## Error handling

Errors include a safe `message`, `failureType`, and nullable `providerCode`.
Here `providerCode` is the upstream HTTP status, not a Meta error code.

- Evolution 401/403/404: `CONFIGURATION`, exposed as HTTP 503.
- Evolution 429/5xx or network timeout: `TEMPORARY`, exposed as HTTP 503.
- Other Evolution 4xx or malformed success: `PERMANENT`, exposed as HTTP 502.
- Missing key/instance: `CONFIGURATION`, exposed as HTTP 503, without an upstream call.
- Invalid HTTP input: HTTP 400 before calling the provider.

Raw upstream error bodies and API keys are not returned. No automatic retries are
performed: a timed-out send might already have been accepted.

## Validation

Run `.\mvnw.cmd clean test` on Windows. Tests cover the HTTP contract, optional
fields, response mapping, error classes, invalid responses and web validation.
All provider responses are simulated; tests do not send messages.

The older planning documents describe the original Meta scope. The current
implementation and runtime setup are documented here.
