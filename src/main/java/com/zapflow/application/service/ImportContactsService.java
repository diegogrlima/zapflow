package com.zapflow.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.zapflow.application.port.in.ContactRow;
import com.zapflow.application.port.in.ImportContactsUseCase;
import com.zapflow.application.port.in.ImportResult;
import com.zapflow.application.port.in.MissingColumnsException;
import com.zapflow.application.port.in.RejectedRow;
import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.domain.contact.Contact;
import com.zapflow.domain.contact.InvalidPhoneNumberException;
import com.zapflow.domain.contact.PhoneNumber;

@Service
public class ImportContactsService implements ImportContactsUseCase {

    static final String NAME_COLUMN = "nome";
    static final String PHONE_COLUMN = "telefone";
    static final String OPT_IN_COLUMN = "opt_in";
    private static final List<String> REQUIRED_COLUMNS = List.of(NAME_COLUMN, PHONE_COLUMN, OPT_IN_COLUMN);
    private static final String OPT_IN_ACCEPTED = "sim";

    private final ImportedContactsPort importedContacts;

    public ImportContactsService(ImportedContactsPort importedContacts) {
        this.importedContacts = importedContacts;
    }

    @Override
    public ImportResult importContacts(List<String> columns, List<ContactRow> rows) {
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(column -> !columns.contains(column))
                .toList();
        if (!missing.isEmpty()) {
            throw new MissingColumnsException(missing);
        }

        List<Contact> contacts = new ArrayList<>();
        List<RejectedRow> rejected = new ArrayList<>();
        // Telefone → linha onde apareceu primeiro, para deduplicar
        Map<PhoneNumber, Integer> firstLineByPhone = new HashMap<>();

        for (ContactRow row : rows) {
            Contact contact;
            try {
                contact = toContact(row);
            } catch (InvalidPhoneNumberException | IllegalArgumentException e) {
                rejected.add(new RejectedRow(row.lineNumber(), e.getMessage()));
                continue;
            }

            Integer firstLine = firstLineByPhone.putIfAbsent(contact.phone(), row.lineNumber());
            if (firstLine != null) {
                rejected.add(new RejectedRow(row.lineNumber(), "Telefone duplicado (linha " + firstLine + ")"));
            } else {
                contacts.add(contact);
            }
        }

        importedContacts.replaceAll(contacts);
        return new ImportResult(contacts.size(), rejected);
    }

    private static Contact toContact(ContactRow row) {
        if (!OPT_IN_ACCEPTED.equalsIgnoreCase(row.value(OPT_IN_COLUMN).strip())) {
            throw new IllegalArgumentException("opt_in diferente de \"sim\"");
        }
        PhoneNumber phone = PhoneNumber.of(row.value(PHONE_COLUMN));
        return new Contact(row.value(NAME_COLUMN), phone, extraFields(row));
    }

    // Tudo que não é coluna obrigatória vira campo extra, na ordem original
    private static Map<String, String> extraFields(ContactRow row) {
        Map<String, String> extras = new LinkedHashMap<>(row.values());
        extras.keySet().removeAll(REQUIRED_COLUMNS);
        return extras;
    }
}
