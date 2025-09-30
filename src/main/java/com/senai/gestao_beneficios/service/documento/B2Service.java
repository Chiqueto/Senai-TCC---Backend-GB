package com.senai.gestao_beneficios.service.documento;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.senai.gestao_beneficios.DTO.documento.DocumentoMapper;
import com.senai.gestao_beneficios.DTO.documento.DocumentoRequestDTO;
import com.senai.gestao_beneficios.DTO.documento.DocumentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.infra.exceptions.IOException;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.infra.exceptions.UnauthorizedException;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.repository.DocumentoRepository;
import com.senai.gestao_beneficios.repository.SolicitacaoRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class B2Service {
    private final AmazonS3 s3Client;
    final DocumentoRepository repository;
    final ColaboradorRepository colaboradorRepository;
    final SolicitacaoRepository solicitacaoRepository;
    final DocumentoMapper documentoMapper;

    @Value("${b2.bucket-name}")
    private String bucketName;

    public ApiResponse<DocumentoResponseDTO> salvarArquivoNoB2(DocumentoRequestDTO documentoRequestDTO) throws java.io.IOException {
        MultipartFile file = documentoRequestDTO.file();

        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio ou inválido.");
        }

        Colaborador colaborador = colaboradorRepository.findById(documentoRequestDTO.colaboradorId()).orElseThrow(() -> new NotFoundException("colaborador", "Colaborador não encontrado"));

        Solicitacao solicitacao = solicitacaoRepository.findByIdAndColaboradorId(documentoRequestDTO.solicitacaoId(), documentoRequestDTO.colaboradorId()).orElseThrow(() -> new UnauthorizedException("Você não tem acesso para enviar documentos nessa solicitação"));

        String nomeOriginal = file.getOriginalFilename();
        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        String nomeUnico = UUID.randomUUID().toString() + extensao;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        s3Client.putObject(bucketName, nomeUnico, file.getInputStream(), metadata);

        Documento documento = new Documento();
        documento.setSolicitacao(solicitacao);
        documento.setUrlArquivo(String.format("https://f005.backblazeb2.com/file/%s/%s", bucketName, nomeUnico));
        documento.setNomeArquivoUnico(nomeUnico);
        documento.setNomeArquivoOriginal(file.getOriginalFilename());
        documento.setTamanho(file.getSize());
        documento.setDataUpload(Instant.now());
        documento.setContentType(file.getContentType());

        Documento documentoFinal = repository.save(documento);

        return new ApiResponse<DocumentoResponseDTO>(true, documentoMapper.toDTO(documentoFinal), null, null, "Documento enviado com sucesso!");
    }
}
