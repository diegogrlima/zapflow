# Modelo de dados (domínio, em memória)

> Nomes iguais aos do código. Sem banco de dados ([0002](decisoes/0002-sem-banco-de-dados.md)). Sem templates da Meta ([0003](decisoes/0003-evolution-api.md)). Estes objetos existem só em memória enquanto a aplicação roda e ficam no centro da hexagonal (`domain` e `application/port`), em Java puro, sem anotações de framework.

```
ImportResult 1───N Contact
ImportResult 1───N RejectedRow
Mensagem     1───N Variavel        (texto com {{coluna}})
Envio        1───1 Mensagem
Envio        1───N ResultadoEnvio
ResultadoEnvio N───1 Contact
```

## Objetos

| Objeto | Campos principais | Observação |
|--------|-------------------|------------|
| **PhoneNumber** (`domain/contact`) | valor (E.164) | *Value object*: só existe se for válido; normaliza `(21) 99999-9999` para `+5521999999999` |
| **Contact** (`domain/contact`) | nome, telefone, campos extras (coluna → valor) | Vem de uma linha do CSV; igual a outro se o telefone for igual |
| **ImportResult** (`port/in`) | quantidade importada, linhas rejeitadas | Relatório da importação; os contatos ficam no `InMemoryContactsAdapter` |
| **RejectedRow** (`port/in`) | número da linha, motivo | Ex.: telefone inválido, duplicado, sem opt-in |
| **Variavel** | nome da coluna (`{{nome}}`) | A fazer (#9); toda variável precisa existir como coluna do CSV |
| **Mensagem** | texto, variáveis | A fazer (#9); sabe gerar o texto final para um contato e a pré-visualização |
| **Envio** | id, mensagem, estado (`EM_ANDAMENTO`, `CONCLUIDO`), total, enviados, falhas, iniciado_em | A fazer (#11); um por vez |
| **ResultadoEnvio** | contato, estado (`ENVIADO`, `FALHOU`), id da mensagem, motivo da falha, tentativas | A fazer (#11) |

## Regras importantes
- Telefone sempre normalizado em E.164 e único dentro da importação.
- Linha sem `opt_in = sim` é rejeitada.
- Toda variável da mensagem precisa existir como coluna do CSV; senão é erro antes de enviar.
- Cada contato recebe no máximo uma mensagem por envio.
- Reiniciar a aplicação apaga tudo: o relatório do envio deve ser baixado ao final.
