package com.zapflow.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.zapflow.application.port.in.ContactRow;
import com.zapflow.application.port.in.ImportResult;
import com.zapflow.application.port.in.MissingColumnsException;
import com.zapflow.application.port.in.RejectedRow;
import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.domain.contact.Contact;
import com.zapflow.domain.contact.PhoneNumber;

class ImportContactsServiceTest {

    private static final List<String> COLUMNS = List.of("nome", "telefone", "opt_in", "cidade");

    private final FakeImportedContactsPort store = new FakeImportedContactsPort();
    private final ImportContactsService service = new ImportContactsService(store);

    @Test
    void importsValidRowsWithExtraFieldsInOriginalOrder() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("nome", "Maria");
        values.put("cidade", "Rio");
        values.put("telefone", "(21) 99999-9999");
        values.put("plano", "Pro");
        values.put("opt_in", "sim");

        ImportResult result = service.importContacts(
                List.of("nome", "cidade", "telefone", "plano", "opt_in"),
                List.of(new ContactRow(2, values)));

        assertThat(result.importedCount()).isEqualTo(1);
        assertThat(result.rejected()).isEmpty();
        Contact maria = store.findAll().getFirst();
        assertThat(maria.name()).isEqualTo("Maria");
        assertThat(maria.phone()).isEqualTo(PhoneNumber.of("+5521999999999"));
        assertThat(maria.extraFields()).containsExactly(
                Map.entry("cidade", "Rio"),
                Map.entry("plano", "Pro"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "SIM", " Sim ", "sim" })
    void acceptsOptInIgnoringCaseAndSpaces(String optIn) {
        ImportResult result = service.importContacts(COLUMNS, List.of(row(2, "Maria", "21999999999", optIn)));

        assertThat(result.importedCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "nao", "não", "", "s", "yes" })
    void rejectsRowWithoutOptInSim(String optIn) {
        ImportResult result = service.importContacts(COLUMNS, List.of(row(2, "Maria", "21999999999", optIn)));

        assertThat(result.importedCount()).isZero();
        assertThat(result.rejected()).containsExactly(new RejectedRow(2, "opt_in diferente de \"sim\""));
        assertThat(store.findAll()).isEmpty();
    }

    @Test
    void rejectsInvalidPhone() {
        ImportResult result = service.importContacts(COLUMNS, List.of(row(3, "Maria", "1234", "sim")));

        assertThat(result.rejected()).containsExactly(new RejectedRow(3, "Telefone inválido: 1234"));
    }

    @Test
    void rejectsBlankName() {
        ImportResult result = service.importContacts(COLUMNS, List.of(row(2, "  ", "21999999999", "sim")));

        assertThat(result.rejected()).containsExactly(new RejectedRow(2, "Nome do contato é obrigatório"));
    }

    @Test
    void rejectsDuplicatePhoneKeepingFirstOccurrence() {
        ImportResult result = service.importContacts(COLUMNS, List.of(
                row(2, "Maria", "(21) 99999-9999", "sim"),
                row(3, "João", "21988888888", "sim"),
                row(4, "Maria de novo", "+55 21 99999-9999", "sim")));

        assertThat(result.importedCount()).isEqualTo(2);
        assertThat(result.rejected()).containsExactly(new RejectedRow(4, "Telefone duplicado (linha 2)"));
        assertThat(store.findAll()).extracting(Contact::name).containsExactly("Maria", "João");
    }

    @Test
    void reportsImportedAndRejectedTogether() {
        ImportResult result = service.importContacts(COLUMNS, List.of(
                row(2, "Maria", "21999999999", "sim"),
                row(3, "João", "21988888888", "nao"),
                row(4, "Ana", "abc", "sim")));

        assertThat(result.importedCount()).isEqualTo(1);
        assertThat(result.rejected()).extracting(RejectedRow::lineNumber).containsExactly(3, 4);
    }

    @Test
    void newImportReplacesPreviousOne() {
        service.importContacts(COLUMNS, List.of(row(2, "Maria", "21999999999", "sim")));
        service.importContacts(COLUMNS, List.of(row(2, "João", "21988888888", "sim")));

        assertThat(store.findAll()).extracting(Contact::name).containsExactly("João");
    }

    @Test
    void failsWhenRequiredColumnsAreMissingAndKeepsPreviousImport() {
        service.importContacts(COLUMNS, List.of(row(2, "Maria", "21999999999", "sim")));

        assertThatThrownBy(() -> service.importContacts(List.of("nome", "cidade"), List.of()))
                .isInstanceOf(MissingColumnsException.class)
                .hasMessage("Colunas obrigatórias ausentes: telefone, opt_in");
        assertThat(store.findAll()).extracting(Contact::name).containsExactly("Maria");
    }

    private static ContactRow row(int lineNumber, String name, String phone, String optIn) {
        return new ContactRow(lineNumber, Map.of("nome", name, "telefone", phone, "opt_in", optIn));
    }

    // Implementação de teste da porta: o serviço é testado sem Spring e sem o adapter real
    private static final class FakeImportedContactsPort implements ImportedContactsPort {

        private List<Contact> contacts = new ArrayList<>();

        @Override
        public void replaceAll(List<Contact> contacts) {
            this.contacts = List.copyOf(contacts);
        }

        @Override
        public List<Contact> findAll() {
            return contacts;
        }
    }
}
