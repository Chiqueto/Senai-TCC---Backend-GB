package com.senai.gestao_beneficios.controller.documento;

import com.senai.gestao_beneficios.DTO.documento.DocumentoRequestDTO;
import com.senai.gestao_beneficios.DTO.documento.DocumentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.service.documento.B2Service;
import com.senai.gestao_beneficios.service.documento.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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
            description = "Faz o upload de um arquivo e o associa a uma solicitação.",
            // Descreve o corpo da requisição multipart para a UI do Swagger
            requestBody = @RequestBody(
                    description = "Arquivo e metadados para o upload.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            // Usa a classe auxiliar para definir os campos do formulário
                            schema = @Schema(implementation = DocumentoUploadRequestSchema.class)
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", // Código HTTP correto para criação de um recurso
                    description = "Documento enviado com sucesso!",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE, // A resposta é um JSON
                            schema = @Schema(implementation = ApiResponse.class) // Aponta para sua classe de resposta padrão
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
            @RequestParam("file") DocumentoRequestDTO documentoRequestDTO
    ) throws IOException {
        ApiResponse<DocumentoResponseDTO> response = b2Service.salvarArquivoNoB2(documentoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}