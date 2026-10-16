# Modelo de dados (domínio, em memória)

> Sem banco de dados ([0002](decisoes/0002-sem-banco-de-dados.md)). Estes objetos existem só em memória enquanto a aplicação roda e ficam no pacote `domain` (Java puro, sem anotações de framework).

```
Importacao 1───N Contato
Importacao 1───N LinhaRejeitada
Mensagem   N───1 Template            (template aprovado lido da Meta)
Mensagem   1───N Variavel
Envio      1───1 Mensagem
Envio      1───N ResultadoEnvio
ResultadoEnvio N───1 Contato
```

## Objetos

| Objeto | Campos principais | Observação |
|--------|-------------------|------------|
| **Telefone** | valor (E.164) | *Value object*: só existe se for válido; normaliza `(21) 99999-9999` para `+5521999999999` |
| **Contato** | nome, telefone, campos extras (coluna → valor) | Vem de uma linha do CSV |
| **Importacao** | contatos válidos, linhas rejeitadas | Resultado da leitura do CSV |
| **LinhaRejeitada** | número da linha, motivo | Ex.: telefone inválido, duplicado, sem opt-in |
| **Template** | nome, idioma, categoria, corpo, quantidade de variáveis | Lido da Meta na hora; só `APPROVED` é aceito |
| **Variavel** | posição (`{{n}}`), origem (valor fixo ou coluna do CSV) | |
| **Mensagem** | template, variáveis | Sabe gerar os parâmetros para um contato e a pré-visualização |
| **Envio** | id, mensagem, estado (`EM_ANDAMENTO`, `CONCLUIDO`), total, enviados, falhas, iniciado_em | Um por vez |
| **ResultadoEnvio** | contato, estado (`ENVIADO`, `FALHOU`), wamid, código de erro, tentativas | |

## Regras importantes
- Telefone sempre normalizado em E.164 e único dentro da importação.
- Linha sem `opt_in = sim` é rejeitada.
- Toda variável do template precisa ter origem; coluna inexistente no CSV é erro antes de enviar.
- Cada contato recebe no máximo uma mensagem por envio.
- Reiniciar a aplicação apaga tudo: o relatório do envio deve ser baixado ao final.
