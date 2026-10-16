package com.zapflow.adapter.in.csv;

import java.util.List;

import com.zapflow.application.port.in.ContactRow;

/**
 * Conteúdo lido do CSV: colunas do cabeçalho (em minúsculas) e linhas de dados.
 */
public record CsvContent(List<String> columns, List<ContactRow> rows) {

    public CsvContent {
        columns = List.copyOf(columns);
        rows = List.copyOf(rows);
    }
}
