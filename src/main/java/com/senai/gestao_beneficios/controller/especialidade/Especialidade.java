package com.senai.gestao_beneficios.controller.especialidade;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.login.LoginResponse;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.infra.exceptions.ServerException;
import com.senai.gestao_beneficios.service.especialidade.EspecialidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/especialidade")
public class Especialidade {
    final EspecialidadeService especialidadeService;

    @PostMapping("")
    @Operation(
            summary = "Realiza o cadastro de uma especialidade",
            description = "Cria uma nova especialidade."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Especialidade criada com sucesso!",
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
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content // Corpo da resposta vazio
            )
    })
    public ResponseEntity<ApiResponse<EspecialidadeDTO>> criarEspecialidade (String nome){
        try{
            ApiResponse<EspecialidadeDTO> response = especialidadeService.createEspecialidade(nome);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (Exception e){
            throw new ServerException(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EspecialidadeDTO>>> buscarEspecialidades(){
        try{
            ApiResponse<List<EspecialidadeDTO>> response = especialidadeService.buscarEspecialidades();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch (Exception e){
            throw new ServerException(e.getMessage());
        }

    }

}
