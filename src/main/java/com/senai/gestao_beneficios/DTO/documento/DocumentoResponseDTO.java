package com.senai.gestao_beneficios.DTO.documento;

import java.time.Instant;

public record DocumentoResponseDTO (
        String arquivoUrl,
        String nomeArquivoUnico,
        String nomeArquivoOriginal,
        Long tamanho,
        Instant dataUpload,
        String assinatura,
        String contentType
) {
}
