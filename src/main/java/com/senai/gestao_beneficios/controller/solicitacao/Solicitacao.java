package com.senai.gestao_beneficios.controller.solicitacao;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoRequestDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoStatusChangeDTO;
import com.senai.gestao_beneficios.service.solicitacao.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{idSolicitacao}")
    @PreAuthorize("hasAuthority('ROLE_GESTAO_BENEFICIOS')")
    @Operation(
            summary = "Altera o status da solicitação",
            description = "Altera o status da solicitação para aprovado ou negado"
    )

    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Solicitação alterada com sucesso!",
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
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content // Corpo da resposta vazio
            )
    })
    public ResponseEntity<ApiResponse<SolicitacaoResponseDTO>> updateSolicitacaoStatus (@RequestBody @Valid SolicitacaoStatusChangeDTO solicitacaoStatusChangeDTO, @Parameter(description = "ID da solicitação que terá o status alterado.", required = true, example = "1") @PathVariable String idSolicitacao) {
        ApiResponse<SolicitacaoResponseDTO> response = service.alterarStatus(idSolicitacao, solicitacaoStatusChangeDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
