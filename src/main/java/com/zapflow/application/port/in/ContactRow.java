package com.zapflow.application.port.in;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Linha de dados do arquivo importado.
 *
 * @param lineNumber linha no arquivo, contando o cabeçalho como linha 1
 * @param values     coluna → valor, na ordem original das colunas
 */
public record ContactRow(int lineNumber, Map<String, String> values) {

    public ContactRow {
        if (lineNumber < 1) {
            throw new IllegalArgumentException("Número da linha deve ser maior que zero");
        }
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    /** Valor da coluna, ou vazio se a linha não tiver essa coluna. */
    public String value(String column) {
        return values.getOrDefault(column, "");
    }
}
