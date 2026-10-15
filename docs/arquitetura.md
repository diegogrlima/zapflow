# Arquitetura lógica

> Independente de tecnologia. A stack será definida em uma etapa posterior.

## Visão geral

```
┌───────────────────┐      ┌──────────────────────┐      ┌──────────────────────────────┐
│ Painel de gestão  │ ───▶ │ API do sistema       │ ───▶ │ Banco de dados               │
│ (contatos,        │      │ (regras de negócio,  │      │ contatos, consentimentos,    │
│  templates,       │      │  validações, auth)   │      │ templates, campanhas,        │
│  campanhas)       │      └──────────┬───────────┘      │ mensagens, eventos, auditoria│
└───────────────────┘                 │                  └──────────────────────────────┘
                                      ▼
                        ┌──────────────────────────────┐
                        │ Fila de envio                │
                        └──────────────┬───────────────┘
                                       ▼
                        ┌──────────────────────────────┐
                        │ Worker de envio              │
                        │ - checa opt-in/bloqueio      │
                        │ - controla ritmo e limite    │
                        │ - retentativa com backoff    │
                        └──────────────┬───────────────┘
                                       ▼
                        ┌──────────────────────────────┐
                        │ WhatsApp Cloud API (Meta)    │
                        └──────────────┬───────────────┘
                                       │ eventos (webhooks)
                                       ▼
                        ┌──────────────────────────────┐
                        │ Receptor de webhooks         │
                        │ - valida assinatura          │
                        │ - grava evento bruto         │
                        │ - processa em segundo plano  │
                        └──────────────────────────────┘
                          atualiza status, opt-outs,
                          qualidade, janela de 24h
```

## Princípios

1. **Envio assíncrono via fila** — permite pausar campanhas, respeitar limites e reprocessar falhas.
2. **Webhook como fonte da verdade** — o estado de cada mensagem só muda a partir dos eventos da Meta.
3. **Regras de consentimento no motor de envio** — nenhuma rota do sistema consegue enviar sem passar pela checagem.
4. **Idempotência** — cada envio tem uma chave única; eventos de webhook são deduplicados pelo ID.
5. **Segredos isolados** — token, segredo do app e verify token só existem no ambiente.

## Fluxos principais

### Envio de campanha
1. Usuário cria a campanha (template aprovado + segmento + variáveis + horário).
2. Sistema resolve o público e descarta contatos sem opt-in da categoria ou bloqueados.
3. Mensagens são enfileiradas com chave de idempotência.
4. Worker consome a fila respeitando throughput e limite diário restante.
5. Cloud API retorna o `wamid`; mensagem vai para `enviada`.
6. Webhooks atualizam para `entregue`, `lida` ou `falhou`.

### Opt-out
1. Usuário responde `SAIR` ou toca no botão de opt-out.
2. Webhook registra a mensagem recebida.
3. Processador identifica a intenção e marca o consentimento como revogado.
4. Contato entra na lista de bloqueio e mensagens pendentes dele são canceladas.
5. Sistema envia confirmação de saída (dentro da janela de 24h aberta pela resposta).

### Ciclo de vida de template
1. Template criado no painel → submetido via Message Templates API.
2. Status `PENDING` até a revisão da Meta.
3. Webhook `message_template_status_update` atualiza para `APPROVED`/`REJECTED`.
4. Webhook de qualidade pode levar a `PAUSED`; campanhas que usam o template são pausadas.

### Proteção de qualidade
- Job periódico consulta limite e nota de qualidade do número.
- Qualidade `YELLOW` → alerta; `RED` → pausa automática de campanhas de marketing.
