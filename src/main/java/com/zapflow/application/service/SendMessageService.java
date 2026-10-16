package com.zapflow.application.service;

import com.zapflow.application.port.in.ContactNotImportedException;
import com.zapflow.application.port.in.SendMessageCommand;
import com.zapflow.application.port.in.SendMessageUseCase;
import com.zapflow.application.port.in.SentMessage;
import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.application.port.out.SendMessagePort;

public class SendMessageService implements SendMessageUseCase {

    private final ImportedContactsPort importedContacts;
    private final SendMessagePort whatsapp;

    public SendMessageService(ImportedContactsPort importedContacts, SendMessagePort whatsapp) {
        this.importedContacts = importedContacts;
        this.whatsapp = whatsapp;
    }

    @Override
    public SentMessage sendMessage(SendMessageCommand command) {
        // Checagem de consentimento: a importação só guarda quem tem opt_in = sim
        boolean imported = importedContacts.findAll().stream()
                .anyMatch(contact -> contact.phone().equals(command.phone()));
        if (!imported) {
            throw new ContactNotImportedException(command.phone());
        }

        String messageId = whatsapp.sendText(command.phone(), command.text());
        return new SentMessage(messageId, command.phone());
    }
}
