# 0003 — Envio pela Evolution API

- **Status:** aceita
- **Data:** 2026-10-16
- **Substitui:** a integração com a WhatsApp Cloud API da Meta prevista no [0001](0001-stack.md) e no [0002](0002-sem-banco-de-dados.md)
- **Responsáveis:** @diegogrlima, @yago1290

## Contexto

O plano inicial era usar a API oficial da Meta (Cloud API), o que exige conta Meta Business verificada, número dedicado, templates aprovados pela Meta e pagamento por conversa. Para o tamanho atual do projeto, esse caminho é pesado. A Evolution API v2 conecta um número de WhatsApp comum por QR Code e expõe uma API HTTP simples para enviar texto (PR #28).

## Decisão

- O envio usa a **Evolution API v2**, rodando em Docker (`docker-compose.yml`) ou em um servidor hospedado.
- O ZapFlow fala com ela por um único adapter: `adapter/out/whatsapp/EvolutionWhatsappAdapter`, que implementa a porta `application/port/out/SendMessagePort`. Nada fora desse pacote conhece o formato da Evolution.
- **Não existe template aprovado pela Meta.** "Montar a mensagem" passa a ser escrever um texto com variáveis `{{coluna}}` preenchidas com as colunas do CSV (issue #9).
- O consentimento continua obrigatório: só recebe mensagem quem veio na última importação, e a importação só aceita `opt_in = sim`.
- PostgreSQL e Redis do `docker-compose.yml` são **da Evolution**, não do ZapFlow. O [0002](0002-sem-banco-de-dados.md) continua valendo: os dados do ZapFlow ficam em memória.

## Consequências

**Positivas**
- Começa a enviar sem conta Meta Business, verificação ou aprovação de templates.
- Mensagem é texto livre, sem custo por conversa da Meta.
- Por causa da hexagonal, voltar para a Cloud API no futuro é escrever outro adapter para a mesma porta; serviços e domínio não mudam.

**Negativas / riscos**
- **Risco de banimento do número.** A Evolution usa o protocolo do WhatsApp Web, que não é oficial para envio em massa. Mitigações: usar um número dedicado (nunca o pessoal), enviar só para quem deu opt-in, ritmo baixo com intervalo entre mensagens (definido na issue #11) e mensagem com instrução de como sair da lista.
- Sem confirmação oficial de entrega: o id devolvido é da Evolution e `PENDING` só significa que ela aceitou.
- Mais infraestrutura para rodar localmente (Evolution, PostgreSQL e Redis no Docker).
- As regras em [regras-whatsapp.md](../regras-whatsapp.md) (limites, categorias e qualidade da Meta) deixam de valer como regra técnica, mas continuam como boas práticas para evitar denúncias.
