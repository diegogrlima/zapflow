# Escopo do projeto

> Revisado em 2026-10-16: o projeto **não usa banco de dados** (ver [decisoes/0002-sem-banco-de-dados.md](decisoes/0002-sem-banco-de-dados.md)).

## Objetivo

Importar uma lista de contatos (CSV), montar uma mensagem a partir de um template aprovado pela Meta e enviá-la para todos os contatos da lista, pela API oficial (WhatsApp Cloud API), respeitando limites e políticas da plataforma.

## O fluxo, de ponta a ponta

1. **Importar contatos** — o usuário envia um CSV com nome, telefone e campos extras. O sistema normaliza os telefones (E.164), remove duplicados e rejeita linhas inválidas, devolvendo um relatório.
2. **Montar a mensagem** — o usuário escolhe um template **aprovado** da sua conta (lido na hora da Meta) e define as variáveis (`{{1}}`, `{{2}}`…) com valores fixos ou colunas do CSV. O sistema mostra uma pré-visualização com um contato real.
3. **Enviar para todos** — o envio roda em segundo plano, com controle de ritmo e retentativa para erros temporários. Ao final, o sistema devolve um relatório por contato (enviado, com o `wamid`, ou falhou, com o código de erro).

Tudo isso vive **em memória** durante a execução da aplicação. Reiniciar a aplicação apaga contatos importados e o progresso de um envio.

## Por que template e não texto livre

Fora da janela de 24h (contato que não falou com a empresa no último dia), a Meta só aceita mensagens iniciadas pelo negócio com **template aprovado**. Por isso "montar a mensagem" é escolher o template e preencher as variáveis, não digitar um texto qualquer. Ver [regras-whatsapp.md](regras-whatsapp.md).

## Fora do escopo

- Banco de dados e qualquer histórico entre execuções (contatos, envios, consentimentos).
- Cadastro manual de contatos (CRUD), tags e segmentos: a lista é sempre o CSV importado.
- Registro e gestão de consentimento dentro do sistema (ver "Consentimento" abaixo).
- Webhooks: status de entrega/leitura, mensagens recebidas, opt-out automático.
- Campanhas agendadas, pausar/retomar envio, painel de métricas e alertas.
- Criação e submissão de templates pelo sistema (criar no Gerenciador do WhatsApp da Meta).
- Chatbot, respostas automáticas e atendimento humano.
- Bibliotecas não oficiais do WhatsApp Web, listas sem opt-in e múltiplos números para contornar limites.

## Consentimento (responsabilidade de quem monta o CSV)

A Meta exige opt-in antes de qualquer mensagem iniciada pelo negócio e exige que a empresa consiga comprová-lo. Sem banco, o ZapFlow **não guarda** essa prova. Regras:

- O CSV deve conter apenas contatos que deram opt-in. A coluna `opt_in` (`sim`/`nao`) é obrigatória: linhas sem `sim` são rejeitadas na importação.
- A prova do opt-in (data, origem, texto aceito) fica fora do sistema, com quem gera o CSV.
- Quem pedir para sair deve ser retirado da planilha de origem antes do próximo envio.
- Templates de marketing devem conter instrução de opt-out.

## Requisitos não funcionais

- Envio sempre assíncrono (a requisição que dispara o envio responde na hora; o envio segue em segundo plano).
- Ritmo configurável (`SEND_RATE_PER_SECOND`) e retentativa com backoff exponencial para erros temporários (ex.: `130429`), até `SEND_MAX_RETRIES`.
- Nenhum contato recebe a mesma mensagem duas vezes no mesmo envio (deduplicação por telefone e uma tentativa bem-sucedida por contato).
- Um envio por vez: não iniciar outro enquanto um estiver em andamento.
- Segredos só em variáveis de ambiente. Logs estruturados com o `wamid` de cada mensagem.
