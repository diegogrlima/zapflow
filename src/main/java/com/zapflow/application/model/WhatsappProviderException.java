package com.zapflow.application.model;

public class WhatsappProviderException extends RuntimeException {
    public enum FailureType { TEMPORARY, PERMANENT, CONFIGURATION }

    private final FailureType failureType;
    private final Integer providerCode;

    public WhatsappProviderException(FailureType failureType, Integer providerCode, String message) {
        super(message);
        this.failureType = failureType;
        this.providerCode = providerCode;
    }

    public FailureType getFailureType() { return failureType; }
    public Integer getProviderCode() { return providerCode; }
}
