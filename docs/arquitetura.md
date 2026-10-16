# Arquitetura

> Stack em [decisoes/0001-stack.md](decisoes/0001-stack.md). Sem banco de dados: [0002](decisoes/0002-sem-banco-de-dados.md). Envio pela Evolution API: [0003](decisoes/0003-evolution-api.md).

O código segue a **Arquitetura Hexagonal** (portas e adaptadores): o domínio e os casos de uso ficam no centro, sem depender de Spring, HTTP, CSV ou da Evolution. Tudo que fala com o mundo de fora é um *adapter* que se liga ao centro por uma *porta* (interface).

## Visão geral

```
         ENTRADA (quem chama o sistema)              SAÍDA (o que o sistema chama)

 ┌──────────────────────────┐                    ┌────────────────────────────────┐
 │ adapter/in/web           │                    │ adapter/out/whatsapp           │
 │  POST /imports (CSV)     │                    │  EvolutionWhatsappAdapter      │
 │  POST /whatsapp/messages │    ┌──────────┐    │  (envia texto pela Evolution)  │
 │  (próximos: mensagem e   │──▶ │ port/in  │    └───────────────▲────────────────┘
 │   envio para todos)      │    └────┬─────┘                    │
 └──────────────────────────┘         ▼                    ┌─────┴─────┐
 ┌──────────────────────────┐   ┌───────────────────┐      │ port/out  │
 │ adapter/in/csv           │   │ application/      │ ───▶ └─────┬─────┘
 │  CsvReader: lê o arquivo │   │ service (casos    │            │
 │  e devolve linhas        │   │ de uso) + domain  │      ┌─────▼──────────────────────┐
 │  (coluna → valor)        │   └───────────────────┘      │ adapter/out/memory         │
 └──────────────────────────┘                              │  InMemoryContactsAdapter   │
                                                           │  (contatos importados)     │
                                                           └────────────────────────────┘
```

## Pacotes (`com.zapflow`)

| Pacote | O que fica aqui | Hoje no código |
|--------|-----------------|----------------|
| `domain/` | Regras de negócio em Java puro, sem anotações de framework | `contact/Contact`, `contact/PhoneNumber` |
| `application/port/in/` | Interfaces dos casos de uso e os dados que entram e saem deles | `ImportContactsUseCase`, `SendMessageUseCase`, `ContactRow`, `ImportResult`, `SendMessageCommand`, `SentMessage` |
| `application/port/out/` | Interfaces do que o caso de uso precisa do mundo de fora | `ImportedContactsPort`, `SendMessagePort`, `WhatsappProviderException` |
| `application/service/` | Implementação dos casos de uso, usando só domínio e portas. **Sem `@Service`** | `ImportContactsService`, `SendMessageService` |
| `adapter/in/web/` | Controllers REST: recebem a requisição, convertem e chamam um `port/in` | `ContactImportController`, `WhatsappController` |
| `adapter/in/csv/` | Leitura do arquivo CSV (colunas, separador, encoding) | `CsvReader` |
| `adapter/out/whatsapp/` | Cliente HTTP da Evolution, implementa `SendMessagePort` | `EvolutionWhatsappAdapter`, `EvolutionProperties` |
| `adapter/out/memory/` | Guarda o estado da execução, implementa as portas de armazenamento | `InMemoryContactsAdapter` |
| `config/` | Montagem do Spring: cria os serviços e liga cada porta ao seu adapter | `UseCaseConfig`, `WhatsappConfig` |

As dependências apontam **só para dentro**: `adapter → application → domain`. O domínio não importa nada dos outros pacotes, e `application` não importa nada de `adapter` nem de Spring.

### Por que os serviços não têm `@Service`
O centro (domain + application) deve funcionar sem Spring, por exemplo num teste de unidade com `new SendMessageService(fakeContatos, fakeWhatsapp)`. Por isso os serviços são classes Java comuns, e quem cria cada um e entrega os adapters certos é o `config/UseCaseConfig`. Adapters podem usar anotações do Spring (`@Component`, `@RestController`), porque eles já são a parte "de fora".

### Nomes
O código usa nomes em inglês (`Contact`, `PhoneNumber`, `SendMessageUseCase`). Os documentos usam esses mesmos nomes para não haver dois nomes para a mesma coisa.

## Fluxos

### 1. Importar contatos (`POST /imports`)
1. `ContactImportController` recebe o arquivo e passa para o `CsvReader`, que devolve as colunas e as linhas (`ContactRow`).
2. `ImportContactsService` exige as colunas `nome`, `telefone` e `opt_in`, cria um `Contact` por linha (telefone normalizado por `PhoneNumber.of`), rejeita quem não tem `opt_in = sim` e descarta duplicados.
3. Os contatos válidos substituem a importação anterior pela porta `ImportedContactsPort`, e o relatório (`ImportResult`: importados + linhas rejeitadas com motivo) volta na resposta.

### 2. Enviar para um número (`POST /whatsapp/messages`)
1. `WhatsappController` converte o telefone com `PhoneNumber.of` e chama `SendMessageUseCase`.
2. `SendMessageService` confere se o telefone está entre os contatos importados (**checagem de consentimento**). Se não estiver, recusa com `ContactNotImportedException`.
3. Chama `SendMessagePort.sendText(telefone, texto)`. O `EvolutionWhatsappAdapter` faz o POST na Evolution e devolve o id da mensagem.

### 3. Montar a mensagem (#9, a fazer)
O usuário escreve um texto com variáveis `{{coluna}}` (ex.: `Olá {{nome}}`). O serviço confere que toda variável existe como coluna do CSV e mostra a pré-visualização com o primeiro contato.

### 4. Enviar para todos (#11, a fazer)
1. A requisição cria o envio e responde na hora (o envio segue em segundo plano).
2. Um executor percorre os contatos com ritmo baixo e intervalo entre mensagens (para reduzir o risco de bloqueio do número) e chama `SendMessagePort` para cada um.
3. Erro `TEMPORARY` → nova tentativa com espera crescente. Erro `PERMANENT` → contato marcado como falhou.
4. Uma rota de consulta mostra o progresso; ao final, o relatório por contato pode ser baixado.

## Princípios

1. **Só contato com opt-in recebe mensagem** — toda rota de envio passa pela lista importada.
2. **Envio em massa assíncrono** — a requisição não espera o envio terminar.
3. **Sem duplicidade** — um contato recebe no máximo uma mensagem por envio.
4. **Segredos isolados** — chave e instância da Evolution só existem em variáveis de ambiente.
5. **Trocar adapter não muda regra** — trocar a Evolution pela Cloud API, ou a memória por um banco, é escrever outro adapter para a mesma porta.
