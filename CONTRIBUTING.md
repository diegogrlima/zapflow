# Como contribuir

## Branches
- `main` — sempre estável; recebe código só via Pull Request.
- `develop` — integração das funcionalidades em andamento.
- Funcionalidades: `feature/<descricao-curta>` (ex.: `feature/opt-out-palavra-chave`)
- Correções: `fix/<descricao-curta>`
- Documentação: `docs/<descricao-curta>`

## Fluxo
1. Pegue (ou crie) uma issue e atribua a você.
2. Crie a branch a partir de `develop`.
3. Faça commits pequenos seguindo o padrão abaixo.
4. Abra o PR para `develop` usando o template e vincule a issue (`Closes #12`).
5. Pelo menos 1 revisão aprovada antes do merge.

## Padrão de commits (Conventional Commits)
```
<tipo>(<escopo opcional>): <descrição no imperativo>
```
Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`.

Exemplos:
- `feat(contatos): adiciona importação em lote`
- `fix(webhook): valida assinatura antes de processar`
- `docs: atualiza regras de limite de envio`

## Regras do projeto
- Nunca versionar tokens, segredos ou o arquivo `.env`.
- Nunca usar dados reais de clientes em testes ou exemplos.
- Toda rota de envio passa pela checagem de consentimento — não criar atalhos.
