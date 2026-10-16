package com.zapflow.adapter.out.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.zapflow.application.port.out.WhatsappProviderException;
import com.zapflow.application.port.out.WhatsappProviderException.FailureType;
import com.zapflow.domain.contact.PhoneNumber;

class EvolutionWhatsappAdapterTest {

    private static final PhoneNumber MARIA = PhoneNumber.of("+5511999999999");
    private static final String ENDPOINT = "http://evolution.test/message/sendText/ZapFlow";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final EvolutionWhatsappAdapter adapter = new EvolutionWhatsappAdapter(builder,
            new EvolutionProperties("http://evolution.test", "ZapFlow", "test-key"));

    @Test
    void sendsDigitsOnlyNumberWithApiKeyAndReturnsMessageId() {
        server.expect(requestTo(ENDPOINT)).andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"number\":\"5511999999999\",\"text\":\"Olá!\"}", JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {"key":{"id":"ABC","remoteJid":"5511999999999@s.whatsapp.net","fromMe":true},
                         "message":{"conversation":"Olá!"},"status":"PENDING","messageTimestamp":123}
                        """, MediaType.APPLICATION_JSON));

        assertEquals("ABC", adapter.sendText(MARIA, "Olá!"));
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({ "400,PERMANENT", "401,CONFIGURATION", "403,CONFIGURATION", "404,CONFIGURATION",
            "429,TEMPORARY", "500,TEMPORARY", "503,TEMPORARY" })
    void classifiesEvolutionHttpFailures(int status, FailureType expected) {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.valueOf(status))
                .contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"private test-key\"}"));

        var error = assertThrows(WhatsappProviderException.class, () -> adapter.sendText(MARIA, "Olá!"));
        assertEquals(expected, error.failureType());
        assertEquals(status, error.providerCode());
        assertFalse(error.getMessage().contains("test-key"));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"key\":{}}", "{\"key\":{\"id\":\"\"}}", "invalid JSON", "" })
    void rejectsInvalidSuccessResponses(String body) {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        assertThrows(WhatsappProviderException.class, () -> adapter.sendText(MARIA, "Olá!"));
        server.verify();
    }

    @Test
    void reportsUnknownDeliveryOnTimeoutWithoutRetrying() {
        server.expect(requestTo(ENDPOINT)).andRespond(withException(new IOException("read timeout")));

        var error = assertThrows(WhatsappProviderException.class, () -> adapter.sendText(MARIA, "Olá!"));
        assertEquals(FailureType.TEMPORARY, error.failureType());
        assertTrue(error.getMessage().contains("pode ter sido entregue"));
        server.verify();
    }

    @Test
    void rejectsMissingConfigurationBeforeHttpCall() {
        var missing = new EvolutionWhatsappAdapter(builder, new EvolutionProperties("http://evolution.test", "", ""));

        var error = assertThrows(WhatsappProviderException.class, () -> missing.sendText(MARIA, "Olá!"));
        assertEquals(FailureType.CONFIGURATION, error.failureType());
        server.verify();
    }
}
