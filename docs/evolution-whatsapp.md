# Integração com a Evolution API

> Decisão em [decisoes/0003-evolution-api.md](decisoes/0003-evolution-api.md).

O ZapFlow envia mensagens pela Evolution API v2, com uma instância de WhatsApp conectada por QR Code.

## Onde fica no código

```text
adapter/in/web/WhatsappController          POST /whatsapp/messages
  -> application/port/in/SendMessageUseCase
  -> application/service/SendMessageService   (só envia para contatos importados)
  -> application/port/out/SendMessagePort     sendText(PhoneNumber, texto) -> id
  -> adapter/out/whatsapp/EvolutionWhatsappAdapter
  -> POST {WHATSAPP_PROVIDER_URL}/message/sendText/{instância}
```

A porta `SendMessagePort` não sabe que a Evolution existe: recebe um `PhoneNumber` (E.164) e um texto e devolve o id da mensagem. O adapter converte `+5521999999999` para `5521999999999`, monta o JSON da Evolution e traduz os erros HTTP para `WhatsappProviderException`.

## Configuração

O Spring lê o `.env` da pasta onde a aplicação roda (ou variáveis de ambiente). Reinicie a aplicação depois de mudar.

```dotenv
WHATSAPP_PROVIDER_URL=http://localhost:8081
WHATSAPP_PROVIDER_INSTANCE=ZapFlow
WHATSAPP_PROVIDER_API_KEY=sua-chave-da-evolution
```

O nome da instância diferencia maiúsculas de minúsculas. A chave vai no header `apikey`. Para a Evolution local no Docker, preencha também `AUTHENTICATION_API_KEY` e `POSTGRES_PASSWORD` (ver `.env.example`).

## Envio de teste para um número

Só funciona para um telefone que veio na última importação de CSV (com `opt_in = sim`).

`POST http://localhost:8080/whatsapp/messages`

```json
{ "phone": "(21) 99999-9999", "text": "Olá do ZapFlow!" }
```

Resposta:

```json
{ "messageId": "ID_NA_EVOLUTION", "phone": "+5521999999999" }
```

Aceito pela Evolution não quer dizer entregue.

## Erros

As respostas de erro seguem o formato `ProblemDetail` (`status`, `detail`).

| Situação | HTTP |
|----------|------|
| Corpo inválido ou telefone inválido | 400 |
| Telefone fora da última importação | 422 |
| Evolution recusou (outros 4xx, resposta inválida) | 502, `failureType: PERMANENT` |
| Evolution 401/403/404 ou instância/chave não configuradas | 503, `failureType: CONFIGURATION` |
| Evolution 429/5xx ou sem resposta | 503, `failureType: TEMPORARY` |

`providerCode` é o status HTTP devolvido pela Evolution. Corpo de erro da Evolution e chaves nunca são devolvidos. Não há retentativa automática: se a Evolution não respondeu, a mensagem pode ter sido entregue.

## Testes

`./mvnw verify` cobre o contrato HTTP, a conversão do telefone, a classificação de erros e a checagem de contato importado. As respostas da Evolution são simuladas; nenhum teste envia mensagem de verdade.
