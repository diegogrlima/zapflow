package com.zapflow.application.service;

import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.SentMessage;
import com.zapflow.application.port.in.SendMessageUseCase;
import com.zapflow.application.port.out.SendMessagePort;

public class SendMessageService implements SendMessageUseCase {
    private final SendMessagePort provider;

    public SendMessageService(SendMessagePort provider) {
        this.provider = provider;
    }

    @Override
    public SentMessage sendMessage(SendMessageCommand command) {
        return provider.sendMessage(command);
    }
}
