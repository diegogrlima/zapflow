package com.zapflow.domain.contact;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contato importado do CSV. A identidade é o telefone (único no sistema).
 */
public final class Contact {

    private final String name;
    private final PhoneNumber phone;
    // Colunas extras do CSV (coluna → valor), na ordem original
    private final Map<String, String> extraFields;

    public Contact(String name, PhoneNumber phone, Map<String, String> extraFields) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do contato é obrigatório");
        }
        this.name = name.strip();
        this.phone = Objects.requireNonNull(phone, "Telefone do contato é obrigatório");
        this.extraFields = copyExtraFields(extraFields);
    }

    public Contact(String name, PhoneNumber phone) {
        this(name, phone, Map.of());
    }

    public String name() {
        return name;
    }

    public PhoneNumber phone() {
        return phone;
    }

    public Map<String, String> extraFields() {
        return extraFields;
    }

    // Cópia defensiva: alterações no map original não afetam o contato
    private static Map<String, String> copyExtraFields(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, String> copy = new LinkedHashMap<>();
        source.forEach((column, value) -> {
            if (column == null || column.isBlank()) {
                throw new IllegalArgumentException("Nome de campo extra não pode ser vazio");
            }
            copy.put(column.strip(), value == null ? "" : value);
        });
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Contact other && phone.equals(other.phone));
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    @Override
    public String toString() {
        return "Contact[name=" + name + ", phone=" + phone + "]";
    }
}
