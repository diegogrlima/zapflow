package com.zapflow.adapter.out.whatsapp.dto;

public record EvolutionSendResponse(Key key, String status) {
    public record Key(String id, String remoteJid, Boolean fromMe) { }
}
