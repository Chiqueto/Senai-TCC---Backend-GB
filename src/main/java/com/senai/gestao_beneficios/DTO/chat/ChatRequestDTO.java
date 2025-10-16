package com.senai.gestao_beneficios.DTO.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDTO (
        @NotBlank(message = "Mensagem deve ter no mínimo 1 caractere")
        String mensagem,
        String conversationId
){
}
