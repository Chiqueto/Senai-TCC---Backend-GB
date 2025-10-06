package com.senai.gestao_beneficios.controller.colaborador;

import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.login.LoginResponse;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.infra.exceptions.ServerException;
import com.senai.gestao_beneficios.service.ColaboradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/colaborador")
public class Colaborador {
    final ColaboradorService colaboradorService;
    @GetMapping("/{id}")
    @Operation(
            summary = "Realiza a busca de um colaborador",
            description = "Busca todos os dados de um colaborador."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Colaborador recuperado com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ColaboradorDTO.class)
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
    public ResponseEntity<ApiResponse<ColaboradorDTO>> getColaboradorById(@PathVariable String id ){
            ApiResponse<ColaboradorDTO> response = colaboradorService.getUserById(id);

            return ResponseEntity.status(HttpStatus.OK).body(response);


    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_GESTAO_BENEFICIOS')")
    @Operation(
            summary = "Realiza a busca de todos os colaboradores",
            description = "Busca todos os dados de todos os colaboradores."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Colaboradores recuperados com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ColaboradorDTO.class)
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
    public ResponseEntity<ApiResponse<List<ColaboradorDTO>>> getColaboradores(){
        ApiResponse<List<ColaboradorDTO>> response = colaboradorService.getAllUsers();

        return ResponseEntity.status(HttpStatus.OK).body(response);


    }
}
