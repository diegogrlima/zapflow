package com.zapflow.domain.contact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "(21) 99999-9999",       // com DDD e máscara
            "21999999999",           // só dígitos
            "21 99999 9999",         // com espaços
            "5521999999999",         // com 55 sem +
            "+55 21 99999-9999",     // com +55 e máscara
            "+5521999999999",        // já em E.164
            "  (21) 99999-9999  "    // espaços nas pontas
    })
    void normalizesMobileToE164(String input) {
        assertThat(PhoneNumber.of(input).value()).isEqualTo("+5521999999999");
    }

    @Test
    void normalizesLandline() {
        assertThat(PhoneNumber.of("(11) 3333-4444").value()).isEqualTo("+551133334444");
    }

    @Test
    void acceptsInternationalNumberWithPlus() {
        assertThat(PhoneNumber.of("+1 (415) 555-2671").value()).isEqualTo("+14155552671");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "abc",
            "99999-9999",            // sem DDD
            "(00) 99999-9999",       // DDD inválido
            "(20) 99999-9999",       // DDD terminado em 0
            "(21) 89999-9999",       // celular com 9 dígitos sem começar com 9
            "(21) 99999-99999",      // dígitos demais
            "+55 21 9999",           // curto demais
            "+1 23"                  // internacional curto demais
    })
    void rejectsInvalidNumbers(String input) {
        assertThatThrownBy(() -> PhoneNumber.of(input))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"5521999999999", "+55 21 99999-9999", "+0021999999999"})
    void constructorRejectsNonE164Values(String value) {
        assertThatThrownBy(() -> new PhoneNumber(value))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    void sameNumberInDifferentFormatsIsEqual() {
        assertThat(PhoneNumber.of("(21) 99999-9999"))
                .isEqualTo(PhoneNumber.of("+5521999999999"));
    }
}
