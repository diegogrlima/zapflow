# Modelo de dados (conceitual)

> Modelo lógico, sem amarração a banco ou ORM.

```
Contato 1───N Consentimento
Contato N───N Tag
Segmento (filtro sobre Contato/Tag)
Template 1───N VariavelTemplate
Campanha N───1 Template
Campanha N───1 Segmento (ou lista manual de Contatos)
Campanha 1───N Mensagem
Mensagem N───1 Contato
Mensagem 1───N EventoMensagem
MensagemRecebida N───1 Contato
EventoWebhook (bruto, para auditoria e reprocessamento)
Auditoria (ações de usuários do painel)
```

## Entidades

| Entidade | Campos principais |
|----------|-------------------|
| **Contato** | id, nome, telefone (E.164, único), campos personalizados, criado_em, ultima_interacao_em |
| **Consentimento** | id, contato_id, categoria (marketing/utilidade), estado (ativo/revogado), origem, texto_aceito, data_opt_in, data_opt_out |
| **Tag** | id, nome |
| **Segmento** | id, nome, regra de filtro |
| **Template** | id, nome_meta, id_meta, categoria, idioma, status, qualidade, componentes (cabeçalho/corpo/rodapé/botões), atualizado_em |
| **VariavelTemplate** | id, template_id, posição, origem (campo do contato ou valor fixo), exemplo |
| **Campanha** | id, nome, template_id, segmento_id, agendada_para, estado (rascunho/agendada/enviando/pausada/concluída/cancelada), criada_por |
| **Mensagem** | id, campanha_id, contato_id, wamid, chave_idempotencia, estado, codigo_erro, enviada_em, entregue_em, lida_em |
| **EventoMensagem** | id, mensagem_id, tipo, payload, recebido_em |
| **MensagemRecebida** | id, contato_id, wamid, tipo, conteúdo, recebida_em |
| **EventoWebhook** | id, id_evento_meta (único), payload bruto, processado, recebido_em |
| **Auditoria** | id, usuario, ação, entidade, entidade_id, data |

## Regras importantes
- Telefone sempre normalizado em E.164 (ex.: `+5521999999999`).
- Consentimento nunca é apagado — muda de estado e mantém histórico.
- `wamid` e `id_evento_meta` são únicos (garantem idempotência).
- Janela de 24h é derivada de `Contato.ultima_interacao_em`.
