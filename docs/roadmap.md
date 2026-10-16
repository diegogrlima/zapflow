# Roadmap

> Revisado em 2026-10-16: escopo reduzido a importar contatos, montar a mensagem e enviar para todos, sem banco de dados ([0002](decisoes/0002-sem-banco-de-dados.md)).

## Fase 0 — Fundação
- [x] Definir stack tecnológica (#1)
- [x] Configurar conta Meta Business, app e número de teste (#2)
- [x] Estrutura inicial do código e pipeline de CI (#3)

## Fase 1 — MVP
- [ ] Contato e telefone E.164 no domínio (#5)
- [ ] Importação de contatos por CSV (#6)
- [ ] Cliente da WhatsApp Cloud API (#8)
- [ ] Montar a mensagem a partir de template aprovado (#9)
- [ ] Envio de teste para um número (#10)
- [ ] Envio para todos os contatos com controle de ritmo e retentativa (#11)

## Fora do escopo atual
Banco e migrations (#4), registro de consentimento (#7), webhooks (#12, #13), campanhas agendadas (#14), tags (#15), opt-out automático (#16), painel (#17), automação (#18, #19) e monitoramento (#20, #21). Essas issues foram fechadas como "not planned" e podem ser reabertas se o escopo crescer.
