package com.zapflow.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.domain.contact.Contact;

@SpringBootTest
@AutoConfigureMockMvc
class ContactImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportedContactsPort importedContacts;

    @Test
    void importsCsvAndReturnsReport() throws Exception {
        String csv = """
                nome;telefone;opt_in;cidade
                Maria;(21) 99999-9999;sim;Rio
                João;21988888888;nao;Niterói
                Ana;21999999999;sim;Rio
                """;

        mockMvc.perform(multipart("/imports").file(csvFile(csv)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.rejected.length()").value(2))
                .andExpect(jsonPath("$.rejected[0].lineNumber").value(3))
                .andExpect(jsonPath("$.rejected[0].reason").value("opt_in diferente de \"sim\""))
                .andExpect(jsonPath("$.rejected[1].lineNumber").value(4))
                .andExpect(jsonPath("$.rejected[1].reason").value("Telefone duplicado (linha 2)"));

        assertThat(importedContacts.findAll()).extracting(Contact::name).containsExactly("Maria");
    }

    @Test
    void returnsBadRequestWhenRequiredColumnsAreMissing() throws Exception {
        mockMvc.perform(multipart("/imports").file(csvFile("nome;cidade\nMaria;Rio\n")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Colunas obrigatórias ausentes: telefone, opt_in"));
    }

    @Test
    void returnsBadRequestForEmptyFile() throws Exception {
        mockMvc.perform(multipart("/imports").file(csvFile("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Arquivo vazio"));
    }

    @Test
    void returnsBadRequestWithoutFile() throws Exception {
        mockMvc.perform(multipart("/imports"))
                .andExpect(status().isBadRequest());
    }

    private static MockMultipartFile csvFile(String content) {
        return new MockMultipartFile("file", "contatos.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }
}
