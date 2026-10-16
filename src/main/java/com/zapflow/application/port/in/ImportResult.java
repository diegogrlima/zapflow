package com.zapflow.application.port.in;

import java.util.List;

/**
 * Relatório da importação: quantos contatos entraram e quais linhas foram recusadas.
 */
public record ImportResult(int importedCount, List<RejectedRow> rejected) {

    public ImportResult {
        if (importedCount < 0) {
            throw new IllegalArgumentException("Quantidade importada não pode ser negativa");
        }
        rejected = List.copyOf(rejected);
    }
}
