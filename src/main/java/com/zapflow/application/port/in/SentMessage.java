package com.zapflow.application.port.in;

import com.zapflow.domain.contact.PhoneNumber;

/**
 * Mensagem aceita pelo provedor de WhatsApp.
 *
 * @param messageId id da mensagem no provedor
 * @param phone     telefone de destino
 */
public record SentMessage(String messageId, PhoneNumber phone) {
}
