package com.zapflow.application.port.in;

import com.zapflow.domain.contact.PhoneNumber;

/**
 * O telefone não está na lista importada (só entra quem tem opt_in = sim), então não pode receber mensagem.
 */
public class ContactNotImportedException extends RuntimeException {

    public ContactNotImportedException(PhoneNumber phone) {
        super("Telefone " + phone + " não está entre os contatos importados com opt_in");
    }
}
