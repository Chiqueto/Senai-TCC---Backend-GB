package com.senai.gestao_beneficios.DTO.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record DocumentoRequestDTO(
        @NotNull(message = "Arquivo não pode ser nulo")
        MultipartFile file,
        @NotBlank(message = "Colaborador deve ser informado")
        String colaboradorId,
        @NotBlank(message = "Solicitação deve ser informada")
        String solicitacaoId
) {
}
