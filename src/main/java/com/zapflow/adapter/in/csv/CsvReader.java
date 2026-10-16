package com.zapflow.adapter.in.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.zapflow.application.port.in.ContactRow;

/**
 * Lê CSV em UTF-8 com separador {@code ,} ou {@code ;} (detectado pelo cabeçalho).
 * Aceita valores entre aspas, com {@code ""} para aspas dentro do valor; não aceita quebra de linha dentro do valor.
 */
@Component
public class CsvReader {

    private static final char QUOTE = '"';
    private static final char BOM = '﻿';

    public CsvContent read(InputStream input) {
        List<String> lines = readLines(input);

        int headerIndex = 0;
        while (headerIndex < lines.size() && lines.get(headerIndex).isBlank()) {
            headerIndex++;
        }
        if (headerIndex == lines.size()) {
            throw new CsvFormatException("Arquivo vazio");
        }

        String headerLine = lines.get(headerIndex);
        char separator = detectSeparator(headerLine);
        List<String> header = parseHeader(headerLine, separator, headerIndex + 1);

        List<ContactRow> rows = new ArrayList<>();
        for (int i = headerIndex + 1; i < lines.size(); i++) {
            int lineNumber = i + 1;
            List<String> values = parseLine(lines.get(i), separator, lineNumber);
            if (values.stream().allMatch(String::isBlank)) {
                continue;
            }
            rows.add(new ContactRow(lineNumber, toColumnMap(header, values, lineNumber)));
        }

        List<String> columns = header.stream().filter(column -> !column.isEmpty()).toList();
        return new CsvContent(columns, rows);
    }

    // UTF-8 estrito: arquivo em outro encoding (ex.: Windows-1252 do Excel) é recusado em vez de virar "Jo�o"
    private static List<String> readLines(InputStream input) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, decoder))) {
            List<String> lines = new ArrayList<>(reader.lines().toList());
            if (!lines.isEmpty() && !lines.get(0).isEmpty() && lines.get(0).charAt(0) == BOM) {
                lines.set(0, lines.get(0).substring(1));
            }
            return lines;
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof CharacterCodingException) {
                throw new CsvFormatException("Arquivo não está em UTF-8");
            }
            throw e;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static char detectSeparator(String headerLine) {
        long semicolons = headerLine.chars().filter(c -> c == ';').count();
        long commas = headerLine.chars().filter(c -> c == ',').count();
        return semicolons > commas ? ';' : ',';
    }

    // Colunas em branco (comuns no fim de planilhas exportadas) ficam como "" e são ignoradas
    private static List<String> parseHeader(String headerLine, char separator, int lineNumber) {
        List<String> header = parseLine(headerLine, separator, lineNumber).stream()
                .map(column -> column.strip().toLowerCase(Locale.ROOT))
                .toList();
        Set<String> seen = new HashSet<>();
        for (String column : header) {
            if (!column.isEmpty() && !seen.add(column)) {
                throw CsvFormatException.atLine(lineNumber, "coluna \"" + column + "\" repetida no cabeçalho");
            }
        }
        return header;
    }

    private static Map<String, String> toColumnMap(List<String> header, List<String> values, int lineNumber) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < Math.max(header.size(), values.size()); i++) {
            String value = i < values.size() ? values.get(i).strip() : "";
            if (i >= header.size()) {
                if (!value.isEmpty()) {
                    throw CsvFormatException.atLine(lineNumber, "mais valores do que colunas no cabeçalho");
                }
            } else if (!header.get(i).isEmpty()) {
                row.put(header.get(i), value);
            }
        }
        return row;
    }

    private static List<String> parseLine(String line, char separator, int lineNumber) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c != QUOTE) {
                    current.append(c);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                    current.append(QUOTE);
                    i++;
                } else {
                    inQuotes = false;
                }
            } else if (c == QUOTE) {
                inQuotes = true;
            } else if (c == separator) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw CsvFormatException.atLine(lineNumber, "aspas não fechadas");
        }
        values.add(current.toString());
        return values;
    }
}
