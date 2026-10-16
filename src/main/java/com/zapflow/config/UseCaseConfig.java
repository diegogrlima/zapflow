package com.zapflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zapflow.application.port.out.ImportedContactsPort;
import com.zapflow.application.port.out.SendMessagePort;
import com.zapflow.application.service.ImportContactsService;
import com.zapflow.application.service.SendMessageService;

/**
 * Cria os casos de uso. Os serviços são Java puro (sem anotação do Spring);
 * é aqui que o Spring entrega a cada um os adapters das portas que ele usa.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    ImportContactsService importContactsService(ImportedContactsPort importedContacts) {
        return new ImportContactsService(importedContacts);
    }

    @Bean
    SendMessageService sendMessageService(ImportedContactsPort importedContacts, SendMessagePort whatsapp) {
        return new SendMessageService(importedContacts, whatsapp);
    }
}
