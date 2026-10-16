package com.zapflow.adapter.in.csv;

public class CsvFormatException extends RuntimeException {

    public CsvFormatException(String message) {
        super(message);
    }

    public static CsvFormatException atLine(int lineNumber, String message) {
        return new CsvFormatException("Linha " + lineNumber + ": " + message);
    }
}
