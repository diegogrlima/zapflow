package com.zapflow.application.port.in;

import java.util.Objects;

import com.zapflow.domain.contact.PhoneNumber;

/**
 * Pedido de envio de um texto para um telefone.
 */
public record SendMessageCommand(PhoneNumber phone, String text) {

    public SendMessageCommand {
        Objects.requireNonNull(phone, "Telefone é obrigatório");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Texto da mensagem é obrigatório");
        }
    }
}
