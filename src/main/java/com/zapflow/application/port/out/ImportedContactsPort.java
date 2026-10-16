package com.zapflow.application.port.out;

import java.util.List;

import com.zapflow.domain.contact.Contact;

/**
 * Guarda os contatos da última importação.
 */
public interface ImportedContactsPort {

    /**
     * Substitui todos os contatos armazenados pela nova lista.
     */
    void replaceAll(List<Contact> contacts);

    /**
     * Retorna os contatos armazenados (lista imutável).
     */
    List<Contact> findAll();
}
