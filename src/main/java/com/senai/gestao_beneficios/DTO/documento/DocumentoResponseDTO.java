package com.senai.gestao_beneficios.DTO.documento;

import com.senai.gestao_beneficios.domain.documento.TipoDocumento;

import java.time.Instant;

public record DocumentoResponseDTO (
        String nomeArquivoUnico,
        String nomeArquivoOriginal,
        Long tamanho,
        Instant dataUpload,
        String contentType,
        TipoDocumento tipoDocumento,
        Instant dataAssinatura
) {
}
