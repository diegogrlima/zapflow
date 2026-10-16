package com.zapflow.adapter.out.memory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.domain.contact.Contact;

@Component
public class InMemoryContactsAdapter implements ImportedContactsPort {

    // Lista imutável trocada por inteiro; volatile garante que todas as threads vejam a nova
    private volatile List<Contact> contacts = List.of();

    @Override
    public void replaceAll(List<Contact> contacts) {
        this.contacts = List.copyOf(contacts);
    }

    @Override
    public List<Contact> findAll() {
        return contacts;
    }
}
