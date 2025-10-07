package com.senai.gestao_beneficios.controller.agendamento;

import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoRequestDTO;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiMeta;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.reponsePattern.Pagination;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.service.agendamento.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agendamento")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class Agendamento {
    final AgendamentoService service;

    @PostMapping()
    @Operation(
            summary = "Cria um novo agendamento",
            description = "Realiza um novo agendamento."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Agendamento criado com sucesso!",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AgendamentoResponseDTO.class)
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
    public ResponseEntity<ApiResponse<AgendamentoResponseDTO>> createAgendamento(@RequestBody @Valid AgendamentoRequestDTO request){
        ApiResponse<AgendamentoResponseDTO> response = service.criarAgendamento(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{colaboradorId}")
    @Operation(
            summary = "Busca os agendamentos de um colaborador",
            description = "Busca os agendamentos de um colaborador."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Agendamentos encontrados com sucesso!",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AgendamentoResponseDTO[].class)
                    )
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
    public ResponseEntity<ApiResponse<List<AgendamentoResponseDTO>>> getAgendamentosByColaborador(@PathVariable String colaboradorId){
        ApiResponse<List<AgendamentoResponseDTO>> response = service.getAgendamentosByColaborador(colaboradorId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_GESTAO_BENEFICIOS')")
    public ResponseEntity<ApiResponse<List<AgendamentoResponseDTO>>> buscarTodosOsAgendamentos(
            @PageableDefault(size = 10, sort = "horario", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AgendamentoResponseDTO> paginaDeAgendamentos = service.getAllAgendamentos(pageable);

        Pagination pagination = new Pagination(
                paginaDeAgendamentos.getNumber(),           // A página atual
                paginaDeAgendamentos.getSize(),             // O tamanho da página
                paginaDeAgendamentos.getTotalElements(),    // O total de elementos
                paginaDeAgendamentos.getTotalPages(),       // O total de páginas
                paginaDeAgendamentos.getSort().toString(),  // Informações de ordenação
                paginaDeAgendamentos.isFirst(),             // É a primeira página?
                paginaDeAgendamentos.isLast(),              // É a última página?
                paginaDeAgendamentos.hasNext(),             // Existe uma próxima página?
                paginaDeAgendamentos.hasPrevious()          // Existe uma página anterior?
        );

        ApiMeta meta = new ApiMeta(pagination);

        ApiResponse<List<AgendamentoResponseDTO>> response = new ApiResponse<>(
                true,
                paginaDeAgendamentos.getContent(), // Extrai a lista de itens da página
                null,
                meta,
                "Agendamentos recuperados com sucesso."
        );

        return ResponseEntity.ok(response);
    }

}
