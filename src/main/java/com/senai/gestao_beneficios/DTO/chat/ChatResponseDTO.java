package com.senai.gestao_beneficios.DTO.chat;

public record ChatResponseDTO(
        String resposta,
        String conversationId // <-- CAMPO NOVO
) {
}
