package com.senai.gestao_beneficios.controller.documento;

import com.senai.gestao_beneficios.service.documento.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Documentos", description = "Endpoints para gerenciamento de documentos") // Opcional, mas organiza a UI
public class Documento { // Renomeado para seguir convenção (ex: DocumentoController)

    final DocumentoService documentoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Realiza o upload de um documento",
            description = "Faz o upload de um arquivo (documento) e uma descrição associada. O arquivo é salvo no servidor."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", // Alterado para 200, pois você retorna ResponseEntity.ok()
                    description = "Upload do documento realizado com sucesso!",
                    content = @Content(
                            mediaType = "text/plain", // A resposta é uma string simples
                            schema = @Schema(type = "string", example = "Upload do arquivo 'exemplo.pdf' realizado com sucesso!")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado.",
                    content = @Content // Corpo da resposta vazio
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado (Forbidden).",
                    content = @Content // Corpo da resposta vazio
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor ao tentar fazer o upload.",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Falha no upload do arquivo: ocorreu um erro inesperado.")
                    )
            )
    })
    public ResponseEntity<String> uploadDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("descricao") String descricao) {
        try {
            // Delega a lógica de salvamento para o Service
            String caminhoDoArquivo = documentoService.salvarArquivo(file);
            System.out.println("Arquivo recebido: " + file.getOriginalFilename());
            System.out.println("Descrição: " + descricao);
            System.out.println("Salvo em: " + caminhoDoArquivo);

            return ResponseEntity.ok("Upload do arquivo '" + file.getOriginalFilename() + "' realizado com sucesso!");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Falha no upload do arquivo: " + e.getMessage());
        }
    }
}