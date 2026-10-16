package com.zapflow.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.zapflow.application.port.in.ContactNotImportedException;
import com.zapflow.application.port.in.SendMessageCommand;
import com.zapflow.application.port.in.SentMessage;
import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.application.port.out.SendMessagePort;
import com.zapflow.domain.contact.Contact;
import com.zapflow.domain.contact.PhoneNumber;

class SendMessageServiceTest {

    private static final PhoneNumber MARIA = PhoneNumber.of("21999999999");
    private static final PhoneNumber JOAO = PhoneNumber.of("21988888888");

    private final FakeImportedContacts importedContacts = new FakeImportedContacts();
    private final FakeWhatsapp whatsapp = new FakeWhatsapp();
    private final SendMessageService service = new SendMessageService(importedContacts, whatsapp);

    @Test
    void sendsToImportedContact() {
        importedContacts.replaceAll(List.of(new Contact("Maria", MARIA)));

        SentMessage sent = service.sendMessage(new SendMessageCommand(MARIA, "Olá!"));

        assertThat(sent).isEqualTo(new SentMessage("msg-1", MARIA));
        assertThat(whatsapp.sent).containsExactly(MARIA + " Olá!");
    }

    @Test
    void refusesPhoneThatWasNotImported() {
        importedContacts.replaceAll(List.of(new Contact("Maria", MARIA)));

        assertThatThrownBy(() -> service.sendMessage(new SendMessageCommand(JOAO, "Olá!")))
                .isInstanceOf(ContactNotImportedException.class);
        assertThat(whatsapp.sent).isEmpty();
    }

    @Test
    void refusesEverythingBeforeAnyImport() {
        assertThatThrownBy(() -> service.sendMessage(new SendMessageCommand(MARIA, "Olá!")))
                .isInstanceOf(ContactNotImportedException.class);
        assertThat(whatsapp.sent).isEmpty();
    }

    private static final class FakeImportedContacts implements ImportedContactsPort {
        private List<Contact> contacts = List.of();

        @Override
        public void replaceAll(List<Contact> contacts) {
            this.contacts = List.copyOf(contacts);
        }

        @Override
        public List<Contact> findAll() {
            return contacts;
        }
    }

    private static final class FakeWhatsapp implements SendMessagePort {
        private final List<String> sent = new ArrayList<>();

        @Override
        public String sendText(PhoneNumber phone, String text) {
            sent.add(phone + " " + text);
            return "msg-" + sent.size();
        }
    }
}
