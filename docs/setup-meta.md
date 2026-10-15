# Configuração da conta na Meta

Passo a passo para obter as credenciais usadas no `.env`.

1. **Meta Business (portfólio)** — crie ou use um portfólio em https://business.facebook.com e inicie a **verificação do negócio** (necessária para subir limites e ter mais templates).
2. **App de desenvolvedor** — em https://developers.facebook.com crie um app do tipo *Business* e adicione o produto **WhatsApp**.
3. **Número de teste** — a Meta fornece um número de teste e permite cadastrar até 5 destinatários para desenvolvimento.
4. **Credenciais**
   - `WHATSAPP_PHONE_NUMBER_ID` e `WHATSAPP_BUSINESS_ACCOUNT_ID`: painel *WhatsApp > API Setup*.
   - `WHATSAPP_ACCESS_TOKEN`: use um **token de usuário do sistema** (System User) com permissões `whatsapp_business_messaging` e `whatsapp_business_management`. O token temporário do painel expira em 24h.
   - `META_APP_SECRET`: *App settings > Basic*. Usado para validar a assinatura dos webhooks.
5. **Webhook**
   - Configure a URL pública do receptor e um `WHATSAPP_WEBHOOK_VERIFY_TOKEN` de sua escolha.
   - Assine os campos: `messages`, `message_template_status_update`, `message_template_quality_update`, `phone_number_quality_update`.
   - Em desenvolvimento, use um túnel HTTPS para expor a máquina local.
6. **Número de produção** — adicione um número próprio (não use seu WhatsApp pessoal) e solicite a aprovação do nome de exibição.
7. **Forma de pagamento** — cadastre no WhatsApp Manager antes de enviar templates em produção.

> Nunca faça commit de tokens. Se um token vazar, revogue-o imediatamente no painel da Meta.
