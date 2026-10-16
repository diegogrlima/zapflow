package com.zapflow.application.model;

public record SentMessage(String messageId, String status, String recipient, boolean fromMe, String text) {
}
