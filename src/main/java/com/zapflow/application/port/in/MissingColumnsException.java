package com.zapflow.application.port.in;

import java.util.List;

public class MissingColumnsException extends RuntimeException {

    private final List<String> missingColumns;

    public MissingColumnsException(List<String> missingColumns) {
        super("Colunas obrigatórias ausentes: " + String.join(", ", missingColumns));
        this.missingColumns = List.copyOf(missingColumns);
    }

    public List<String> missingColumns() {
        return missingColumns;
    }
}
