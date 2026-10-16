package com.zapflow.adapter.in.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.zapflow.application.port.in.ContactRow;

class CsvReaderTest {

    private final CsvReader reader = new CsvReader();

    @ParameterizedTest
    @ValueSource(strings = { ",", ";" })
    void readsBothSeparators(String separator) {
        CsvContent csv = read(String.join(separator, "nome", "telefone", "opt_in") + "\n"
                + String.join(separator, "Maria", "21999999999", "sim") + "\n");

        assertThat(csv.columns()).containsExactly("nome", "telefone", "opt_in");
        assertThat(csv.rows()).containsExactly(new ContactRow(2,
                Map.of("nome", "Maria", "telefone", "21999999999", "opt_in", "sim")));
    }

    @Test
    void normalizesHeaderNamesAndStripsValues() {
        CsvContent csv = read(" Nome ;TELEFONE;Opt_In\n  Maria ; 21999999999 ;sim\n");

        assertThat(csv.columns()).containsExactly("nome", "telefone", "opt_in");
        assertThat(csv.rows().getFirst().value("nome")).isEqualTo("Maria");
        assertThat(csv.rows().getFirst().value("telefone")).isEqualTo("21999999999");
    }

    @Test
    void keepsFileLineNumbersAndSkipsBlankLines() {
        CsvContent csv = read("nome,telefone,opt_in\r\nMaria,1,sim\r\n\r\n,,\r\nJoão,2,sim\r\n");

        assertThat(csv.rows()).extracting(ContactRow::lineNumber).containsExactly(2, 5);
    }

    @Test
    void keepsColumnOrder() {
        CsvContent csv = read("cidade;nome;telefone;opt_in;plano\nRio;Maria;1;sim;Pro\n");

        assertThat(csv.rows().getFirst().values().keySet())
                .containsExactly("cidade", "nome", "telefone", "opt_in", "plano");
    }

    @Test
    void readsQuotedValuesWithSeparatorsAndEscapedQuotes() {
        CsvContent csv = read("nome;telefone;opt_in;obs\n\"Silva; Maria\";1;sim;\"disse \"\"oi\"\"\"\n");

        assertThat(csv.rows().getFirst().value("nome")).isEqualTo("Silva; Maria");
        assertThat(csv.rows().getFirst().value("obs")).isEqualTo("disse \"oi\"");
    }

    @Test
    void detectsSemicolonWhenHeaderAlsoHasComma() {
        CsvContent csv = read("nome;telefone;opt_in;\"cidade, UF\"\nMaria;1;sim;\"Rio, RJ\"\n");

        assertThat(csv.columns()).containsExactly("nome", "telefone", "opt_in", "cidade, uf");
    }

    @Test
    void readsUtf8WithBomAndAccents() {
        CsvContent csv = read("﻿nome;telefone;opt_in\nJoão;1;sim\n");

        assertThat(csv.columns()).containsExactly("nome", "telefone", "opt_in");
        assertThat(csv.rows().getFirst().value("nome")).isEqualTo("João");
    }

    @Test
    void padsMissingValuesAndIgnoresBlankTrailingColumns() {
        CsvContent csv = read("nome;telefone;opt_in;;\nMaria;1;;;\n");

        assertThat(csv.columns()).containsExactly("nome", "telefone", "opt_in");
        assertThat(csv.rows().getFirst().values())
                .containsExactly(Map.entry("nome", "Maria"), Map.entry("telefone", "1"), Map.entry("opt_in", ""));
    }

    @Test
    void rejectsFileNotInUtf8() {
        byte[] latin1 = "nome;telefone;opt_in\nJoão;1;sim\n".getBytes(Charset.forName("windows-1252"));

        assertThatThrownBy(() -> reader.read(new ByteArrayInputStream(latin1)))
                .isInstanceOf(CsvFormatException.class)
                .hasMessage("Arquivo não está em UTF-8");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "\n\n", "﻿" })
    void rejectsEmptyFile(String content) {
        assertThatThrownBy(() -> read(content))
                .isInstanceOf(CsvFormatException.class)
                .hasMessage("Arquivo vazio");
    }

    @Test
    void rejectsDuplicateColumns() {
        assertThatThrownBy(() -> read("nome;telefone;Nome\n"))
                .isInstanceOf(CsvFormatException.class)
                .hasMessage("Linha 1: coluna \"nome\" repetida no cabeçalho");
    }

    @Test
    void rejectsUnclosedQuote() {
        assertThatThrownBy(() -> read("nome;telefone;opt_in\n\"Maria;1;sim\n"))
                .isInstanceOf(CsvFormatException.class)
                .hasMessage("Linha 2: aspas não fechadas");
    }

    @Test
    void rejectsRowWithMoreValuesThanColumns() {
        assertThatThrownBy(() -> read("nome,telefone,opt_in\nSilva, Maria,1,sim\n"))
                .isInstanceOf(CsvFormatException.class)
                .hasMessage("Linha 2: mais valores do que colunas no cabeçalho");
    }

    private CsvContent read(String content) {
        return reader.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }
}
