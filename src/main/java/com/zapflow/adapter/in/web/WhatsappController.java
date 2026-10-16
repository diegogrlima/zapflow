package com.zapflow.adapter.in.web;

import com.zapflow.adapter.in.web.dto.SendMessageRequest;
import com.zapflow.adapter.in.web.dto.SendMessageResponse;
import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.WhatsappProviderException;
import com.zapflow.application.port.in.SendMessageUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WhatsappController {
    private final SendMessageUseCase sender;

    public WhatsappController(SendMessageUseCase sender) {
        this.sender = sender;
    }

    @PostMapping("/whatsapp/messages")
    public SendMessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        var result = sender.sendMessage(new SendMessageCommand(request.number(), request.textMessage().text(),
                request.delay(), request.quoted(), request.linkPreview(), request.mentioned()));
        return new SendMessageResponse(new SendMessageResponse.Key(result.messageId(), result.recipient(), result.fromMe()),
                new SendMessageResponse.Message(result.text()), result.status());
    }

    public record ProviderError(String message, String failureType, Integer providerCode) { }

    @ExceptionHandler(WhatsappProviderException.class)
    public ResponseEntity<ProviderError> handleProviderError(WhatsappProviderException exception) {
        var status = exception.getFailureType() == WhatsappProviderException.FailureType.PERMANENT
                ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(new ProviderError(exception.getMessage(),
                exception.getFailureType().name(), exception.getProviderCode()));
    }
}
