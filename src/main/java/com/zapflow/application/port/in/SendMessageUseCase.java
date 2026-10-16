package com.zapflow.application.port.in;

public interface SendMessageUseCase {

    /**
     * Envia um texto para um contato da última importação.
     *
     * @throws ContactNotImportedException se o telefone não estiver entre os contatos importados com opt_in
     */
    SentMessage sendMessage(SendMessageCommand command);
}
