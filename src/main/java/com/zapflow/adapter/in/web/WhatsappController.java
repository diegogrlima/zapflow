package com.zapflow.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zapflow.adapter.in.web.dto.SendMessageRequest;
import com.zapflow.adapter.in.web.dto.SendMessageResponse;
import com.zapflow.application.port.in.ContactNotImportedException;
import com.zapflow.application.port.in.SendMessageCommand;
import com.zapflow.application.port.in.SendMessageUseCase;
import com.zapflow.application.port.out.WhatsappProviderException;
import com.zapflow.domain.contact.InvalidPhoneNumberException;
import com.zapflow.domain.contact.PhoneNumber;

import jakarta.validation.Valid;

@RestController
public class WhatsappController {

    private final SendMessageUseCase sendMessage;

    public WhatsappController(SendMessageUseCase sendMessage) {
        this.sendMessage = sendMessage;
    }

    @PostMapping("/whatsapp/messages")
    public SendMessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        var command = new SendMessageCommand(PhoneNumber.of(request.phone()), request.text());
        var sent = sendMessage.sendMessage(command);
        return new SendMessageResponse(sent.messageId(), sent.phone().value());
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    ProblemDetail handleInvalidPhone(InvalidPhoneNumberException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // Sem opt_in não há envio: o número precisa ter vindo na última importação
    @ExceptionHandler(ContactNotImportedException.class)
    ProblemDetail handleNotImported(ContactNotImportedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(WhatsappProviderException.class)
    ProblemDetail handleProviderError(WhatsappProviderException e) {
        var status = e.failureType() == WhatsappProviderException.FailureType.PERMANENT
                ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
        var problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setProperty("failureType", e.failureType().name());
        problem.setProperty("providerCode", e.providerCode());
        return problem;
    }
}
