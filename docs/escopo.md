# Escopo do projeto

> Revisado em 2026-10-16: o projeto **não usa banco de dados** ([0002](decisoes/0002-sem-banco-de-dados.md)) e envia pela **Evolution API** ([0003](decisoes/0003-evolution-api.md)).

## Objetivo

Importar uma lista de contatos (CSV), montar uma mensagem com variáveis do CSV e enviá-la para todos os contatos da lista pela Evolution API, só para quem deu opt-in e num ritmo que não coloque o número em risco.

## O fluxo, de ponta a ponta

1. **Importar contatos** — o usuário envia um CSV com nome, telefone e campos extras. O sistema normaliza os telefones (E.164), remove duplicados e rejeita linhas inválidas, devolvendo um relatório.
2. **Montar a mensagem** — o usuário escreve o texto com variáveis `{{coluna}}` (ex.: `Olá {{nome}}`), que são preenchidas com as colunas do CSV. O sistema confere que toda variável existe e mostra uma pré-visualização com um contato real.
3. **Enviar para todos** — o envio roda em segundo plano, com ritmo baixo, intervalo entre mensagens e retentativa para erros temporários. Ao final, o sistema devolve um relatório por contato (enviado, com o id da mensagem, ou falhou, com o motivo).

Tudo isso vive **em memória** durante a execução da aplicação. Reiniciar a aplicação apaga contatos importados e o progresso de um envio.

## Por que o ritmo importa

A Evolution usa um número de WhatsApp comum, conectado por QR Code. Muitas mensagens seguidas, para pessoas que não esperam receber, levam a denúncias e ao bloqueio do número. Por isso o envio é lento, só para quem deu opt-in e com instrução de como sair da lista. Ver [regras-whatsapp.md](regras-whatsapp.md).

## Fora do escopo

- Banco de dados e qualquer histórico entre execuções (contatos, envios, consentimentos).
- Cadastro manual de contatos (CRUD), tags e segmentos: a lista é sempre o CSV importado.
- Registro e gestão de consentimento dentro do sistema (ver "Consentimento" abaixo).
- Webhooks: status de entrega/leitura, mensagens recebidas, opt-out automático.
- Campanhas agendadas, pausar/retomar envio, painel de métricas e alertas.
- Templates aprovados pela Meta e qualquer integração direta com a Cloud API.
- Chatbot, respostas automáticas e atendimento humano.
- Listas sem opt-in e múltiplos números para contornar bloqueios.

## Consentimento (responsabilidade de quem monta o CSV)

Mandar mensagem só para quem pediu é a regra da LGPD e a melhor proteção contra o bloqueio do número. Sem banco, o ZapFlow **não guarda** essa prova. Regras:

- O CSV deve conter apenas contatos que deram opt-in. A coluna `opt_in` (`sim`/`nao`) é obrigatória: linhas sem `sim` são rejeitadas na importação.
- A prova do opt-in (data, origem, texto aceito) fica fora do sistema, com quem gera o CSV.
- Quem pedir para sair deve ser retirado da planilha de origem antes do próximo envio.
- Mensagens de divulgação devem conter instrução de como sair da lista.

## Requisitos não funcionais

- Envio sempre assíncrono (a requisição que dispara o envio responde na hora; o envio segue em segundo plano).
- Ritmo configurável e baixo, com intervalo entre mensagens, e retentativa com espera crescente para erros temporários (`TEMPORARY`), com limite de tentativas.
- Nenhum contato recebe a mesma mensagem duas vezes no mesmo envio (deduplicação por telefone e uma tentativa bem-sucedida por contato).
- Um envio por vez: não iniciar outro enquanto um estiver em andamento.
- Segredos só em variáveis de ambiente. Logs com o id de cada mensagem devolvido pela Evolution.
