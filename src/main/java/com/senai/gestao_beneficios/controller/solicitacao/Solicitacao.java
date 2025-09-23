package com.senai.gestao_beneficios.controller.solicitacao;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoRequestDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.service.solicitacao.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/solicitacao")
public class Solicitacao {
    final SolicitacaoService service;

    @PostMapping("")
    @Operation(
            summary = "Realiza o cadastro de uma solicitação",
            description = "Cria uma nova solicitação."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Solicitação criada com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SolicitacaoResponseDTO.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado.",
                    content = @Content // Corpo da resposta vazio
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Não autenticado.",
                    content = @Content // Corpo da resposta vazio
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Solicitação já existe.",
                    content = @Content // Corpo da resposta vazio
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content // Corpo da resposta vazio
            )
    })
    public ResponseEntity<ApiResponse<SolicitacaoResponseDTO>> createSolicitacao (@RequestBody @Valid SolicitacaoRequestDTO solicitacaoRequestDTO){
        ApiResponse<SolicitacaoResponseDTO> response = service.criarSolicitacao(solicitacaoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
