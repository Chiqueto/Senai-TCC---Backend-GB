package com.senai.gestao_beneficios.DTO.documento;

import com.senai.gestao_beneficios.domain.documento.Documento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentoMapper {

    public DocumentoResponseDTO toDTO(Documento documento) {
        if (documento == null) {
            return null;
        }

        return new DocumentoResponseDTO(
                documento.getNomeArquivoUnico(),
                documento.getNomeArquivoOriginal(),
                documento.getTamanho(),
                documento.getDataUpload(),
                documento.getContentType(),
                documento.getTipo(),
                documento.getDataAssinatura()
        );
    }

    public List<DocumentoResponseDTO> toDTOList(List<Documento> documentos) {
        return documentos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}