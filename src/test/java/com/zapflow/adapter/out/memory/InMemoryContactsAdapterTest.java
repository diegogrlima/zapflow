package com.zapflow.adapter.out.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.zapflow.domain.contact.Contact;
import com.zapflow.domain.contact.PhoneNumber;

class InMemoryContactsAdapterTest {

    private static final Contact MARIA = new Contact("Maria", PhoneNumber.of("21999999999"));
    private static final Contact JOAO = new Contact("João", PhoneNumber.of("21988888888"));

    private final InMemoryContactsAdapter adapter = new InMemoryContactsAdapter();

    @Test
    void startsEmpty() {
        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void replaceAllReplacesPreviousContacts() {
        adapter.replaceAll(List.of(MARIA));
        adapter.replaceAll(List.of(JOAO));

        assertThat(adapter.findAll()).containsExactly(JOAO);
    }

    @Test
    void storedContactsAreProtectedFromExternalChanges() {
        List<Contact> originalContacts = new ArrayList<>(List.of(MARIA));
        adapter.replaceAll(originalContacts);

        // Alterar a lista original não deve alterar os contatos armazenados
        originalContacts.add(JOAO);

        assertThat(adapter.findAll()).containsExactly(MARIA);
        assertThatThrownBy(() -> adapter.findAll().add(JOAO))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
