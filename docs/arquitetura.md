# Arquitetura

> Stack em [decisoes/0001-stack.md](decisoes/0001-stack.md). Sem banco de dados: [decisoes/0002-sem-banco-de-dados.md](decisoes/0002-sem-banco-de-dados.md).

O código segue a **Arquitetura Hexagonal** (portas e adaptadores): o domínio e os casos de uso ficam no centro, sem depender de Spring, HTTP, CSV ou da Meta. Tudo que fala com o mundo de fora é um *adapter* que se liga ao centro por uma *porta* (interface).

## Visão geral

```
         ENTRADA (quem chama o sistema)              SAÍDA (o que o sistema chama)

 ┌──────────────────────────┐                    ┌────────────────────────────────┐
 │ adapter/in/web           │                    │ adapter/out/whatsapp           │
 │  POST /importacoes (CSV) │                    │  cliente da Cloud API (envio,  │
 │  POST /mensagem          │    ┌──────────┐    │  listar templates aprovados)   │
 │  POST /envios            │──▶ │ port/in  │    └───────────────▲────────────────┘
 │  GET  /envios/atual      │    └────┬─────┘                    │
 └──────────────────────────┘         ▼                    ┌─────┴─────┐
 ┌──────────────────────────┐   ┌───────────────────┐      │ port/out  │
 │ adapter/in/csv           │   │ application/      │ ───▶ └─────┬─────┘
 │  lê o arquivo e vira     │   │ service (casos    │            │
 │  linhas para o caso de   │   │ de uso) + domain  │      ┌─────▼──────────────────────┐
 │  uso de importação       │   └───────────────────┘      │ adapter/out/memoria        │
 └──────────────────────────┘                              │  guarda contatos, mensagem │
                                                           │  e progresso do envio      │
                                                           └────────────────────────────┘
```

## Pacotes (`com.zapflow`)

| Pacote | O que fica aqui | Exemplo |
|--------|-----------------|---------|
| `domain/` | Regras de negócio em Java puro, sem anotações de framework | `Telefone`, `Contato`, `Importacao`, `Mensagem`, `Envio`, `ResultadoEnvio` |
| `application/port/in/` | Interfaces dos casos de uso (o que o sistema sabe fazer) | `ImportarContatosUseCase`, `MontarMensagemUseCase`, `EnviarParaTodosUseCase` |
| `application/port/out/` | Interfaces do que o caso de uso precisa do mundo de fora | `EnviarTemplatePort`, `ListarTemplatesPort`, `ContatosImportadosPort`, `EnvioAtualPort` |
| `application/service/` | Implementação dos casos de uso, usando só domínio e portas | `ImportarContatosService`, `EnviarParaTodosService` |
| `adapter/in/web/` | Controllers REST: recebem a requisição e chamam um `port/in` | `ImportacaoController` (upload multipart) |
| `adapter/in/csv/` | Leitura do arquivo CSV (colunas, separador, encoding) | `LeitorCsv` |
| `adapter/out/whatsapp/` | Cliente HTTP da Cloud API, implementa as portas de envio e de templates | `WhatsAppCloudApiAdapter` |
| `adapter/out/memoria/` | Guarda o estado da execução, implementa as portas de armazenamento | `ContatosEmMemoriaAdapter` |
| `config/` | Configuração do Spring (beans, propriedades `zapflow.*`, executor do envio) | `EnvioConfig` |

As dependências apontam **só para dentro**: `adapter → application → domain`. O domínio não importa nada dos outros pacotes.

## Fluxos

### 1. Importar contatos
1. `ImportacaoController` recebe o arquivo e passa para o `LeitorCsv`, que devolve linhas (coluna → valor).
2. `ImportarContatosService` cria um `Contato` por linha: valida `Telefone` (E.164), exige `opt_in = sim` e descarta duplicados.
3. A `Importacao` (válidos + rejeitados) é guardada pela porta `ContatosImportadosPort` e o relatório volta na resposta.

### 2. Montar a mensagem
1. `MontarMensagemService` busca os templates `APPROVED` pela porta `ListarTemplatesPort` (adapter da Meta).
2. O usuário escolhe um template e diz a origem de cada `{{n}}` (valor fixo ou coluna do CSV).
3. O serviço valida que todas as variáveis têm origem e devolve a pré-visualização com o primeiro contato.

### 3. Enviar para todos
1. `POST /envios` cria o `Envio` e responde na hora (o envio segue em segundo plano).
2. Um executor percorre os contatos respeitando `SEND_RATE_PER_SECOND` e chama `EnviarTemplatePort` para cada um.
3. Erro temporário (ex.: `130429`) → nova tentativa com backoff exponencial até `SEND_MAX_RETRIES`. Erro definitivo → `FALHOU` com o código.
4. `GET /envios/atual` mostra o progresso; ao final, o relatório por contato pode ser baixado.

## Princípios

1. **Envio assíncrono** — a requisição não espera o envio terminar.
2. **Só template aprovado** — mensagem iniciada pela empresa sempre usa template `APPROVED`.
3. **Sem duplicidade** — um contato recebe no máximo uma mensagem por envio.
4. **Segredos isolados** — token e IDs da Meta só existem em variáveis de ambiente.
5. **Trocar adapter não muda regra** — se um dia houver banco, só nasce `adapter/out/persistencia`; domínio e serviços ficam iguais.
