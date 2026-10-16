package com.zapflow.adapter.in.web;

import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.SentMessage;
import com.zapflow.application.model.WhatsappProviderException;
import com.zapflow.application.service.SendMessageService;
import com.zapflow.application.port.out.SendMessagePort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WhatsappControllerTests {
    private final SendMessagePort provider = mock(SendMessagePort.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new WhatsappController(new SendMessageService(provider))).build();
    private final String body = "{\"number\":\"5511999999999\",\"textMessage\":{\"text\":\"Hello!\"}}";

    @Test
    void preservesEndpointAndNestedTextContractThroughUseCase() throws Exception {
        when(provider.sendMessage(any())).thenReturn(new SentMessage("ABC", "PENDING", "recipient@s.whatsapp.net", true, "Hello!"));
        mvc.perform(post("/whatsapp/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.key.id").value("ABC"))
                .andExpect(jsonPath("$.message.conversation").value("Hello!"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        verify(provider).sendMessage(new SendMessageCommand("5511999999999", "Hello!", null, Map.of(), null, List.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"number\":\"123\"}",
            "{\"number\":\"123\",\"textMessage\":{\"text\":\"\"}}",
            "{\"number\":\"123\",\"textMessage\":{\"text\":\"Hello\"},\"delay\":-1}"})
    void validatesBeforeCallingProvider(String body) throws Exception {
        mvc.perform(post("/whatsapp/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(provider);
    }

    @ParameterizedTest
    @CsvSource({"TEMPORARY,503", "PERMANENT,502", "CONFIGURATION,503"})
    void returnsSafeStructuredErrors(WhatsappProviderException.FailureType type, int status) throws Exception {
        when(provider.sendMessage(any())).thenThrow(new WhatsappProviderException(type, 400, "Evolution error"));
        mvc.perform(post("/whatsapp/messages").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(status)).andExpect(jsonPath("$.failureType").value(type.name()))
                .andExpect(jsonPath("$.providerCode").value(400));
    }
}
