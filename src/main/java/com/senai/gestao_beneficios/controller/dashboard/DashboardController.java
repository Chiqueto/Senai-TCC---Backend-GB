package com.senai.gestao_beneficios.controller.dashboard;

import com.senai.gestao_beneficios.DTO.Dashboard.DashboardResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.service.Dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('ROLE_GESTAO_BENEFICIOS')")
    @Operation(summary = "Retorna um resumo de dados agregados para o dashboard")
    public ResponseEntity<ApiResponse<DashboardResponseDTO>> getResumo(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes
    ) {
        int anoParaBusca = (ano != null) ? ano : LocalDate.now().getYear();

        DashboardResponseDTO resumo = dashboardService.getDashboardResumo(anoParaBusca, mes);

        ApiResponse<DashboardResponseDTO> response = new ApiResponse<>(
                true, resumo, null, null, "Resumo do dashboard recuperado com sucesso."
        );

        return ResponseEntity.ok(response);
    }
}