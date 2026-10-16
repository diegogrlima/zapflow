package com.zapflow.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;

public record SendMessageRequest(@NotBlank String number, @NotNull @Valid TextMessage textMessage,
                                 @PositiveOrZero Integer delay, Map<String, Object> quoted,
                                 Boolean linkPreview, List<@NotBlank String> mentioned) {
    public record TextMessage(@NotBlank String text) { }
}
