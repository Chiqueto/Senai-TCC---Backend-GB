package com.senai.gestao_beneficios.DTO.chat;

public record  ChatResponseDTO(
        String resposta,
        String conversationId,
        String nextAction,  // <-- NOVO
        Object pendingData
) {
    public ChatResponseDTO(String resposta, String conversationId) {
        this(resposta, conversationId, null, null);
    }
}
