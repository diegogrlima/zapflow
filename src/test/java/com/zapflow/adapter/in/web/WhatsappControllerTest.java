package com.zapflow.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.zapflow.application.port.in.ContactNotImportedException;
import com.zapflow.application.port.in.SendMessageCommand;
import com.zapflow.application.port.in.SendMessageUseCase;
import com.zapflow.application.port.in.SentMessage;
import com.zapflow.application.port.out.WhatsappProviderException;
import com.zapflow.domain.contact.PhoneNumber;

class WhatsappControllerTest {

    private static final PhoneNumber MARIA = PhoneNumber.of("21999999999");

    private final SendMessageUseCase useCase = mock(SendMessageUseCase.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new WhatsappController(useCase)).build();

    @Test
    void normalizesPhoneAndCallsUseCase() throws Exception {
        when(useCase.sendMessage(any())).thenReturn(new SentMessage("ABC", MARIA));

        send("{\"phone\":\"(21) 99999-9999\",\"text\":\"Olá!\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("ABC"))
                .andExpect(jsonPath("$.phone").value("+5521999999999"));
        verify(useCase).sendMessage(new SendMessageCommand(MARIA, "Olá!"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"phone\":\"21999999999\"}", "{\"phone\":\"21999999999\",\"text\":\"\"}",
            "{\"phone\":\"123\",\"text\":\"Olá\"}" })
    void rejectsInvalidRequestBeforeCallingUseCase(String body) throws Exception {
        send(body).andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void refusesPhoneOutsideImportedContacts() throws Exception {
        when(useCase.sendMessage(any())).thenThrow(new ContactNotImportedException(MARIA));

        send("{\"phone\":\"21999999999\",\"text\":\"Olá\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(
                        "Telefone +5521999999999 não está entre os contatos importados com opt_in"));
    }

    @ParameterizedTest
    @CsvSource({ "TEMPORARY,503", "PERMANENT,502", "CONFIGURATION,503" })
    void returnsProviderErrorsWithoutDetailsFromProvider(WhatsappProviderException.FailureType type, int status)
            throws Exception {
        when(useCase.sendMessage(any())).thenThrow(new WhatsappProviderException(type, 400, "Erro da Evolution"));

        send("{\"phone\":\"21999999999\",\"text\":\"Olá\"}")
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.failureType").value(type.name()))
                .andExpect(jsonPath("$.providerCode").value(400));
    }

    private org.springframework.test.web.servlet.ResultActions send(String body) throws Exception {
        return mvc.perform(post("/whatsapp/messages").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
