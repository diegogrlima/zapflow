package com.zapflow.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param phone telefone em qualquer formato aceito na importação (ex.: "(21) 99999-9999" ou "+5521999999999")
 * @param text  texto da mensagem
 */
public record SendMessageRequest(@NotBlank String phone, @NotBlank String text) {
}
