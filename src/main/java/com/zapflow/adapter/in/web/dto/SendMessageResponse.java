package com.zapflow.adapter.in.web.dto;

public record SendMessageResponse(Key key, Message message, String status) {
    public record Key(String id, String remoteJid, boolean fromMe) { }
    public record Message(String conversation) { }
}
