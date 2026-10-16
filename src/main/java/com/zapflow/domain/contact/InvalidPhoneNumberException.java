package com.zapflow.domain.contact;

public class InvalidPhoneNumberException extends RuntimeException {

    public InvalidPhoneNumberException(String value) {
        super("Telefone inválido: " + value);
    }
}
