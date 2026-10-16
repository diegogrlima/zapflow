package com.zapflow.domain.contact;

import java.util.regex.Pattern;

/**
 * Value object de telefone, sempre em E.164 (ex.: +5521999999999).
 * Se a instância existe, o valor é válido.
 */
public record PhoneNumber(String value) {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final String BRAZIL_CODE = "55";
    // DDD (11–99, sem zero) + celular (9 dígitos começando com 9) ou fixo (8 dígitos começando com 2–5)
    private static final Pattern BRAZILIAN_NATIONAL = Pattern.compile("^[1-9][1-9](9\\d{8}|[2-5]\\d{7})$");

    public PhoneNumber {
        if (value == null || !E164.matcher(value).matches()) {
            throw new InvalidPhoneNumberException(value);
        }
    }

    /** Normaliza a entrada "suja" (CSV, formulário) para E.164. */
    public static PhoneNumber of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidPhoneNumberException(raw);
        }

        String trimmed = raw.strip();
        String digits = trimmed.replaceAll("\\D", "");

        // Com "+" e DDI diferente de 55: número internacional, vale só a validação E.164
        if (trimmed.startsWith("+") && !digits.startsWith(BRAZIL_CODE)) {
            return new PhoneNumber("+" + digits);
        }

        // Sem DDI: assume Brasil
        String national = switch (digits.length()) {
            case 10, 11 -> digits;
            case 12, 13 -> digits.startsWith(BRAZIL_CODE) ? digits.substring(2) : null;
            default -> null;
        };

        if (national == null || !BRAZILIAN_NATIONAL.matcher(national).matches()) {
            throw new InvalidPhoneNumberException(raw);
        }
        return new PhoneNumber("+" + BRAZIL_CODE + national);
    }

    @Override
    public String toString() {
        return value;
    }
}
