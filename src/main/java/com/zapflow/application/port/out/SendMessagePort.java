package com.zapflow.application.port.out;

import com.zapflow.domain.contact.PhoneNumber;

/**
 * Envia mensagens pelo WhatsApp. Não sabe qual provedor está por trás (hoje, a Evolution API).
 */
public interface SendMessagePort {

    /**
     * Envia um texto e devolve o id da mensagem no provedor.
     *
     * @throws WhatsappProviderException se o provedor recusar ou não responder
     */
    String sendText(PhoneNumber phone, String text);
}
