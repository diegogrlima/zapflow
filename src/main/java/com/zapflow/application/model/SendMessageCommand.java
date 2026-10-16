package com.zapflow.application.model;

import java.util.List;
import java.util.Map;

public record SendMessageCommand(String number, String text, Integer delay,
                                 Map<String, Object> quoted, Boolean linkPreview, List<String> mentioned) {
    public SendMessageCommand {
        if (number == null || number.isBlank() || text == null || text.isBlank()) {
            throw new IllegalArgumentException("Number and text are required");
        }
        if (delay != null && delay < 0) throw new IllegalArgumentException("Delay cannot be negative");
        quoted = quoted == null ? Map.of() : Map.copyOf(quoted);
        mentioned = mentioned == null ? List.of() : List.copyOf(mentioned);
    }
}
