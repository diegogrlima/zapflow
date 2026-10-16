package com.zapflow.application.port.in;

import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.SentMessage;

public interface SendMessageUseCase {
    SentMessage sendMessage(SendMessageCommand command);
}
