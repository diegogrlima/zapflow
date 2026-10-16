package com.zapflow.adapter.in.web;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zapflow.adapter.in.csv.CsvContent;
import com.zapflow.adapter.in.csv.CsvFormatException;
import com.zapflow.adapter.in.csv.CsvReader;
import com.zapflow.application.port.in.ImportContactsUseCase;
import com.zapflow.application.port.in.ImportResult;
import com.zapflow.application.port.in.MissingColumnsException;

@RestController
public class ContactImportController {

    private final CsvReader csvReader;
    private final ImportContactsUseCase importContacts;

    public ContactImportController(CsvReader csvReader, ImportContactsUseCase importContacts) {
        this.csvReader = csvReader;
        this.importContacts = importContacts;
    }

    @PostMapping(path = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importContacts(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new CsvFormatException("Arquivo vazio");
        }
        try (InputStream input = file.getInputStream()) {
            CsvContent csv = csvReader.read(input);
            return importContacts.importContacts(csv.columns(), csv.rows());
        }
    }

    // Arquivo inválido como um todo: nada é importado
    @ExceptionHandler({ CsvFormatException.class, MissingColumnsException.class })
    ProblemDetail handleInvalidFile(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
