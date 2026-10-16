package com.zapflow.domain.contact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactTest {

    private static final PhoneNumber PHONE = PhoneNumber.of("(21) 99999-9999");

    @Test
    void createsContactWithExtraFieldsInOriginalOrder() {
        Map<String, String> extras = new LinkedHashMap<>();
        extras.put("cidade", "Rio");
        extras.put("plano", "Pro");

        Contact contact = new Contact("  Maria  ", PHONE, extras);

        assertThat(contact.name()).isEqualTo("Maria");
        assertThat(contact.phone()).isEqualTo(PHONE);
        assertThat(contact.extraFields()).containsExactly(
                Map.entry("cidade", "Rio"),
                Map.entry("plano", "Pro"));
    }

    @Test
    void extraFieldsAreProtectedFromExternalChanges() {
        Map<String, String> extras = new HashMap<>(Map.of("cidade", "Rio"));
        Contact contact = new Contact("Maria", PHONE, extras);

        extras.put("plano", "Pro");

        assertThat(contact.extraFields()).doesNotContainKey("plano");
        assertThatThrownBy(() -> contact.extraFields().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullExtraFieldValueBecomesEmpty() {
        Map<String, String> extras = new HashMap<>();
        extras.put("cidade", null);

        assertThat(new Contact("Maria", PHONE, extras).extraFields()).containsEntry("cidade", "");
    }

    @Test
    void contactWithoutExtraFields() {
        assertThat(new Contact("Maria", PHONE).extraFields()).isEmpty();
        assertThat(new Contact("Maria", PHONE, null).extraFields()).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> new Contact(name, PHONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullPhone() {
        assertThatThrownBy(() -> new Contact("Maria", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankExtraFieldName() {
        assertThatThrownBy(() -> new Contact("Maria", PHONE, Map.of(" ", "x")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contactsWithSamePhoneAreEqual() {
        assertThat(new Contact("Maria", PHONE))
                .isEqualTo(new Contact("Maria Silva", PhoneNumber.of("+5521999999999")));
    }
}
