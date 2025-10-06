package com.senai.gestao_beneficios.controller.documento;

import com.senai.gestao_beneficios.DTO.documento.DocumentoRequestDTO;
import com.senai.gestao_beneficios.DTO.documento.DocumentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.service.documento.B2Service;
import com.senai.gestao_beneficios.service.documento.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documentos")
@Tag(name = "Documentos", description = "Endpoints para gerenciamento de documentos")
public class Documento {

    private final B2Service b2Service;
    private final DocumentoService documentoService;

    // Classe auxiliar para que o Swagger possa gerar a documentação da UI corretamente
    private static class DocumentoUploadRequestSchema {
        @Schema(type = "string", format = "binary", description = "O arquivo a ser enviado.")
        public MultipartFile file;

        // Adicione aqui outros campos que seu DTO possa ter, por exemplo:
        @Schema(type = "string", example = "a1b2c3d4-...", description = "ID da solicitação associada.")
        public String solicitacaoId;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Realiza o upload de um documento",
            description = "Faz o upload de um arquivo e o associa a uma solicitação existente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Documento enviado com sucesso!",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Requisição inválida (ex: dados ausentes)", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Não autorizado.", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Acesso negado.", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "Erro interno no servidor", content = @Content
            )
    })
    public ResponseEntity<ApiResponse<DocumentoResponseDTO>> uploadDocumento(
            @RequestParam("file")
            @Parameter(description = "O arquivo a ser enviado.",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            MultipartFile file,

            @RequestParam("solicitacaoId")
            @Parameter(description = "ID da solicitação à qual o documento pertence.")
            String solicitacaoId,

            @RequestParam("colaboradorId")
            @Parameter(description = "ID do colaborador à qual a solicitação pertence.")
            String colaboradorId
    ) throws IOException {
        ApiResponse<DocumentoResponseDTO> response = b2Service.salvarArquivoNoB2(file, solicitacaoId, colaboradorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{idSolicitacao}")
    @Operation(
            summary = "Realiza a busca dos documentos",
            description = "Faz a busca de todos os documentos referents a uma solicitação existente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Documento encontrados com sucesso!",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Bad Requeste", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Não autorizado.", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Acesso negado.", content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "Erro interno no servidor", content = @Content
            )
    })
    public ResponseEntity<ApiResponse<List<DocumentoResponseDTO>>>  getDocumentosBySolicitacao (
            @RequestParam("solicitacaoId")
            @Parameter(description = "ID da solicitação à qual o documento pertence.")
            String solicitacaoId,
            @RequestParam("colabordaorId")
            @Parameter(description = "ID do colaborador à qual a solicitação pertence.")
            String colaboradorId
    ) {
        ApiResponse<List<DocumentoResponseDTO>> response = documentoService.getAllDocumentsBySolicitacao(solicitacaoId, colaboradorId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{nomeArquivoUnico}/url-acesso")
    @Operation(summary = "Gera uma URL de acesso temporária para um documento")

    public ResponseEntity<ApiResponse<URL>> getUrlDeAcesso(@PathVariable String nomeArquivoUnico) {

            ApiResponse<URL> response = b2Service.gerarUrlPresignadaParaLeitura(nomeArquivoUnico);

            return ResponseEntity.status(HttpStatus.OK).body(response);

    }


}