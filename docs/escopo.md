# Escopo do projeto

## Objetivo

Permitir que um negócio envie mensagens padronizadas pelo WhatsApp para contatos selecionados que deram consentimento, de forma automatizada, rastreável e dentro das políticas da Meta.

## Fora do escopo

- Uso de bibliotecas/automação não oficiais do WhatsApp Web ou do app comum.
- Envio para contatos sem opt-in ("listas compradas", contatos frios).
- Chatbot de IA de propósito geral sem finalidade de negócio (proibido pela política da Meta).
- Múltiplos números/contas para contornar limites de envio.

## Módulos

### 1. Onboarding e conta
- Conta Meta Business verificada, WhatsApp Business Account (WABA) e número dedicado.
- Nome de exibição aprovado.
- Armazenamento seguro de token de acesso, `phone_number_id`, `waba_id` e segredo do app.

### 2. Gestão de contatos
- CRUD de contatos (nome, telefone em formato E.164, campos personalizados).
- Importação em lote com validação e deduplicação de telefone.
- Tags e segmentos dinâmicos (filtros).
- Seleção manual de contatos ou por segmento para uma campanha.

### 3. Consentimento
- Registro de opt-in: data/hora, origem (formulário, presencial, resposta no WhatsApp…), texto aceito e categorias autorizadas (marketing, utilidade).
- Opt-out por palavra-chave (`SAIR`, `PARAR`, `STOP`) ou botão de resposta rápida.
- Lista de bloqueio consultada **obrigatoriamente** pelo motor de envio.
- Histórico auditável (nunca apagar o registro do consentimento, apenas mudar o estado).

### 4. Templates
- Cadastro local espelhando os templates da Meta: nome, categoria, idioma, corpo, cabeçalho, rodapé, botões e variáveis.
- Submissão para aprovação via API e sincronização de status (`PENDING`, `APPROVED`, `REJECTED`, `PAUSED`, `DISABLED`).
- Mapeamento de variáveis (`{{1}}`, `{{2}}`…) para campos do contato ou valores fixos da campanha.
- Pré-visualização da mensagem com dados de um contato real.
- Templates de marketing devem conter instrução de opt-out.

### 5. Campanhas e envio
- Campanha = template + público + parâmetros + agendamento.
- Pré-validação de cada destinatário:
  - possui opt-in ativo para a categoria do template?
  - não está na lista de bloqueio?
  - template está `APPROVED`?
  - cabe no limite diário restante do portfólio?
- Fila de envio assíncrona com controle de ritmo (throughput) e retentativa com backoff exponencial para erros temporários (ex.: `130429`).
- Pausar, retomar e cancelar campanha.
- Estados por mensagem: `enfileirada → enviada → entregue → lida` ou `falhou` (com código de erro).

### 6. Webhooks e conversas
- Verificação do endpoint (challenge) e validação da assinatura `X-Hub-Signature-256`.
- Processamento idempotente de eventos (podem chegar duplicados ou fora de ordem).
- Registro de mensagens recebidas e abertura/fechamento da janela de atendimento de 24h.
- Detecção de opt-out em mensagens recebidas.
- Atualizações de qualidade e status de templates.

### 7. Automação conversacional
- Respostas automáticas por palavra-chave.
- Menus com botões / listas interativas.
- Encaminhamento para atendimento humano.

### 8. Monitoramento
- Painel: enviadas, entregues, lidas, falhas, opt-outs, nota de qualidade, uso do limite diário, custo estimado por categoria.
- Alertas: queda de qualidade, template pausado, limite próximo do fim, taxa de falha alta.
- Pausa automática de campanhas quando a qualidade cair.

### 9. Segurança e LGPD
- Segredos fora do código (variáveis de ambiente / cofre).
- Autenticação e perfis de acesso no painel.
- Logs de auditoria (quem criou/disparou campanhas, quem alterou consentimentos).
- Retenção mínima de dados e exclusão a pedido do titular.

## Requisitos não funcionais

- Envio sempre assíncrono (nunca em loop síncrono na requisição do usuário).
- Webhook deve responder rapidamente (200) e processar em segundo plano.
- Idempotência no envio (não duplicar mensagem em retentativas).
- Observabilidade: logs estruturados e correlação por ID de mensagem da Meta (`wamid`).
