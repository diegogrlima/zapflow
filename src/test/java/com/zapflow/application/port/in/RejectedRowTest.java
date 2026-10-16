package com.zapflow.application.port.in;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RejectedRowTest {

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void rejectsLineNumberBelowOne(int lineNumber) {
        assertThatThrownBy(() -> new RejectedRow(lineNumber, "motivo"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void rejectsBlankReason(String reason) {
        assertThatThrownBy(() -> new RejectedRow(2, reason))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsValidRow() {
        new RejectedRow(2, "opt_in diferente de \"sim\"");
    }
}
