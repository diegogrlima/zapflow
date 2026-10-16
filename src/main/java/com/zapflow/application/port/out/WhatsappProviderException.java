package com.zapflow.application.port.out;

/**
 * Falha do provedor de WhatsApp, classificada para o caso de uso decidir o que fazer.
 */
public class WhatsappProviderException extends RuntimeException {

    public enum FailureType {
        /** Vale tentar de novo mais tarde (provedor fora do ar, limite de envio). */
        TEMPORARY,
        /** Tentar de novo não resolve (destinatário ou mensagem recusados). */
        PERMANENT,
        /** Configuração errada do ZapFlow (URL, instância ou chave). */
        CONFIGURATION
    }

    private final FailureType failureType;
    private final Integer providerCode;

    public WhatsappProviderException(FailureType failureType, Integer providerCode, String message) {
        super(message);
        this.failureType = failureType;
        this.providerCode = providerCode;
    }

    public FailureType failureType() {
        return failureType;
    }

    /** Código HTTP devolvido pelo provedor, ou {@code null} se não houve resposta. */
    public Integer providerCode() {
        return providerCode;
    }
}
