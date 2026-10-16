package com.zapflow.application.port.in;

/**
 * Linha do arquivo recusada na importação.
 *
 * @param lineNumber linha no arquivo, contando o cabeçalho como linha 1
 * @param reason     motivo exibido para o usuário
 */
public record RejectedRow(int lineNumber, String reason) {

    public RejectedRow {
        if (lineNumber < 1) {
            throw new IllegalArgumentException("Número da linha deve ser maior que zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo da rejeição é obrigatório");
        }
    }
}
