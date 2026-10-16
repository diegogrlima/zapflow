# Regras e limitações da WhatsApp Business Platform

> **Atenção:** desde o [0003](decisoes/0003-evolution-api.md) o ZapFlow envia pela Evolution API, não pela Cloud API. As regras abaixo são da plataforma oficial da Meta: limites, categorias e templates não se aplicam tecnicamente, mas janela, opt-in, opt-out e qualidade continuam sendo as melhores práticas para o número não ser denunciado e bloqueado.
>
> Levantamento feito em outubro de 2026. As regras da Meta mudam com frequência — sempre confira a documentação oficial antes de decisões de produção.

## Janela de atendimento (24h)
- Quando o usuário envia uma mensagem, abre-se uma janela de 24h em que o negócio pode responder livremente.
- Fora da janela, só é possível iniciar conversa com **template aprovado**.

## Categorias de template
| Categoria | Uso | Observação |
|-----------|-----|------------|
| Marketing | Promoções, ofertas, novidades | Mais cara; template de "utilidade" com conteúdo promocional é reclassificado como marketing |
| Utilidade | Confirmações, atualizações de pedido, lembretes | Ajuda o cliente a concluir algo que ele já iniciou |
| Autenticação | Códigos de verificação / login | Marketing e utilidade não podem conter códigos de login |

- Limite de criação: 100 templates por hora por WABA.
- Quantidade máxima: 250 templates (portfólio não verificado) ou até 6.000 (verificado).
- Template com qualidade baixa é pausado automaticamente.

## Consentimento (opt-in)
- Obrigatório antes de enviar mensagens iniciadas pelo negócio.
- Deve ser ativo (ação do usuário: marcar caixa, informar número etc.) e cumprir a legislação local (LGPD).
- O negócio precisa conseguir comprovar como o opt-in foi obtido.
- Boas práticas: opt-in separado por categoria e instruções claras de opt-out, honradas imediatamente.

## Limites de envio
- Contam destinatários **únicos** contatados fora da janela de atendimento em 24h móveis.
- Desde outubro/2025 o limite é por **portfólio de negócio** e compartilhado entre todos os números.
- Portfólio novo começa com **250**; o limite sobe automaticamente com bom uso e boa qualidade.
- Consulta via Phone Number API: campo `whatsapp_business_manager_messaging_limit`.

## Throughput
- Padrão: até 80 mensagens/segundo por número (upgrade automático até 1.000 com limite ilimitado e qualidade ≥ média).
- Excedeu → erro `130429`; tratar com retentativa e backoff.

## Qualidade
- Bloqueios e denúncias reduzem a nota (verde → amarelo → vermelho).
- Nota baixa: limites reduzidos, restrições temporárias e, em casos graves, suspensão da conta.
- Opt-out visível no template reduz denúncias.

## Custos
- Cobrança por mensagem de template **entregue**, variando por categoria e país do destinatário (desde julho/2025).
- A partir de 01/10/2026, mensagens de atendimento e templates de utilidade dentro da janela de 24h também são cobrados (com franquia mensal de mensagens de atendimento).
- Meta só altera preços no primeiro dia de cada trimestre.

## IA
- Permitido: automação com finalidade de negócio clara (atendimento, vendas, suporte).
- Proibido: chatbots de propósito geral, IA que se passa por "amigo", bots sem finalidade comercial.

## Práticas anti-bloqueio (checklist)
- [ ] Só enviar para quem tem opt-in ativo da categoria.
- [ ] Opt-out visível em todos os templates de marketing.
- [ ] Aquecer o número aumentando o volume gradualmente.
- [ ] Começar campanhas pelos contatos mais engajados.
- [ ] Mensagens relevantes e personalizadas; evitar frequência excessiva.
- [ ] Pausar automaticamente quando a qualidade cair.
- [ ] Nunca criar múltiplas contas/números para contornar limites.

## Referências oficiais
- Templates: https://developers.facebook.com/documentation/business-messaging/whatsapp/templates/overview
- Limites: https://developers.facebook.com/documentation/business-messaging/whatsapp/messaging-limits
- Throughput: https://developers.facebook.com/documentation/business-messaging/whatsapp/throughput
- Opt-in: https://developers.facebook.com/documentation/business-messaging/whatsapp/getting-opt-in
- Preços: https://developers.facebook.com/documentation/business-messaging/whatsapp/pricing
