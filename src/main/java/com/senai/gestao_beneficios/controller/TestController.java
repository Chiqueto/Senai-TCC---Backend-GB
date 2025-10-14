package com.senai.gestao_beneficios.controller;

import com.senai.gestao_beneficios.service.documento.DocumentoGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "Z_Testes", description = "Endpoints temporários para desenvolvimento e depuração")
public class TestController {

    private final DocumentoGenerationService documentoGenerationService;

    @GetMapping(value = "/autorizacao-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Gera um PDF de autorização para teste de layout",
            description = "Endpoint APENAS PARA DESENVOLVIMENTO. Gera um PDF de autorização com dados falsos para verificar o layout, fontes e imagens."
    )
    @ApiResponse(
            responseCode = "200",
            description = "PDF gerado. O Swagger UI fornecerá um link para download.",
            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))
    )
    public ResponseEntity<byte[]> testarPdfAutorizacao() {
        try {
            byte[] pdfBytes = documentoGenerationService.gerarPdfReciboParaTeste();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "teste_autorizacao.pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}