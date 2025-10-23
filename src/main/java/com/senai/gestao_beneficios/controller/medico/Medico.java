package com.senai.gestao_beneficios.controller.medico;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoAvaiabilityDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.service.medico.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medico")
public class Medico {
    final MedicoService service;

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_GESTAO_BENEFICIOS')")
    @Operation(
            summary = "Realiza o cadastro de um médico",
            description = "Cria um novo médico."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Médico criada com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EspecialidadeDTO.class)
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
                    description = "Médico já existe.",
                    content = @Content // Corpo da resposta vazio
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content // Corpo da resposta vazio
            )
    })
    public ResponseEntity<ApiResponse<MedicoResponseDTO>> createMedico (@RequestBody @Valid MedicoRequestDTO medicoRequestDTO){
            ApiResponse<MedicoResponseDTO> response = service.createMedico(medicoRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("")
    @Operation(
            summary = "Busca todos os médicos cadastrados",
            description = "Busca todos os médicos cadastrados."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Médicos buscados com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MedicoResponseDTO[].class)
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
    public ResponseEntity<ApiResponse<List<MedicoResponseDTO>>> getAllMedicos (){
            ApiResponse<List<MedicoResponseDTO>> response = service.getMedicos();
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{idMedico}/disponibilidade")
    @Operation(summary = "Busca os horários disponíveis de um médico para um dia específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidades buscadas com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MedicoAvaiabilityDTO[].class)
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
    public ResponseEntity<ApiResponse<List<MedicoAvaiabilityDTO>>> getAvaiability (@PathVariable
                                                                                       @Parameter(description = "ID do médico")
                                                                                       String idMedico,

                                                                                   @RequestParam
                                                                                       @Parameter(description = "Dia para verificar a disponibilidade (formato: AAAA-MM-DD)")
                                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                                       LocalDate dia){
        ApiResponse<List<MedicoAvaiabilityDTO>> response = service.buscarDisponibilidade(idMedico, dia);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
