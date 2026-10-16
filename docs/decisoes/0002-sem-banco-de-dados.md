# 0002 — Sem banco de dados

- **Status:** aceita
- **Data:** 2026-10-16
- **Substitui:** as linhas "Banco" e "Fila de envio" do [0001](0001-stack.md)
- **Responsável:** @diegogrlima

## Contexto

O objetivo do ZapFlow foi reduzido a três passos: importar contatos de um CSV, montar a mensagem a partir de um template aprovado e enviá-la para todos os contatos. Não há necessidade de cadastro permanente, campanhas, histórico ou painel.

## Decisão

- **Nenhum banco de dados.** Contatos importados, a mensagem montada e o progresso do envio ficam **em memória** enquanto a aplicação roda.
- **Fila de envio em memória**, dentro da própria aplicação: um único envio por vez, consumido em segundo plano com controle de ritmo (`SEND_RATE_PER_SECOND`) e retentativa com backoff (`SEND_MAX_RETRIES`). Sem PostgreSQL, Flyway, Spring Data ou broker.
- Os templates são lidos da Meta (Message Templates API) quando o usuário monta a mensagem, sem cópia local.
- O resultado do envio é devolvido como relatório (por exemplo, CSV para download) ao final.

Na arquitetura hexagonal, a memória é só mais um adapter de saída: as portas (`port/out`) continuam existindo, então trocar a memória por um banco no futuro não muda o domínio nem os serviços.

## Consequências

**Positivas**
- Nada de infraestrutura além da própria aplicação: roda com `./mvnw spring-boot:run`.
- Menos código e menos issues no MVP.

**Negativas / riscos**
- **Sem prova de opt-in no sistema.** A Meta exige que a empresa comprove o consentimento; essa prova passa a ser responsabilidade de quem monta o CSV (coluna `opt_in` obrigatória).
- **Sem retomada.** Se a aplicação cair no meio do envio, o progresso se perde; reenviar a mesma lista pode mandar mensagem duplicada para quem já recebeu. Mitigação: baixar o relatório parcial e reimportar só os que faltaram.
- **Sem opt-out automático nem status de entrega**, porque isso depende de webhooks e de guardar uma lista de bloqueio entre execuções.
- **Limite diário da Meta não é rastreado entre execuções**; o envio só descobre o limite pelo erro da API.
