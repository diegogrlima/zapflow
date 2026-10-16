package com.zapflow.application.port.out;

import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.SentMessage;

public interface SendMessagePort {
    SentMessage sendMessage(SendMessageCommand command);
}
