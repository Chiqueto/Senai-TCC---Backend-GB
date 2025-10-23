package com.senai.gestao_beneficios.service.Dashboard;

import com.senai.gestao_beneficios.DTO.Dashboard.*;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.infra.exceptions.DateTimeException;
import com.senai.gestao_beneficios.repository.AgendamentoRepository;
import com.senai.gestao_beneficios.repository.SolicitacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    public DashboardResponseDTO getDashboardResumo(int ano, Integer mes) {
        if (mes < 1 || mes > 12){
            throw new DateTimeException("Mês informado inválido. Mês deve estar entre 1 e 12");
        }

        KpiDTO kpis = calcularKpis(ano, mes);
        List<ConsultasPorMesDTO> consultasPorMes = calcularConsultasPorMes(ano);
        List<SolicitacoesPorBeneficioDTO> solicitacoesPorBeneficio = calcularSolicitacoesPorBeneficio();

        return new DashboardResponseDTO(kpis, consultasPorMes, solicitacoesPorBeneficio);
    }

    private KpiDTO calcularKpis(int ano, Integer mes) {
        long beneficiosPendentes = solicitacaoRepository.countByStatus(StatusSolicitacao.PENDENTE);
        long solicitacoesPendenteAssinatura = solicitacaoRepository.countByStatus(StatusSolicitacao.PENDENTE_ASSINATURA);
        long solicitacoesRecusadas = solicitacaoRepository.countByStatus(StatusSolicitacao.RECUSADA);

        long consultasPendentes = agendamentoRepository.countByStatusAndHorarioAfter(StatusAgendamento.AGENDADO, Instant.now());

        YearMonth mesDeReferencia = (mes != null)
                ? YearMonth.of(ano, mes)
                : YearMonth.now(FUSO_HORARIO_NEGOCIO);


        Instant inicioDoMes = mesDeReferencia.atDay(1).atStartOfDay(FUSO_HORARIO_NEGOCIO).toInstant();
        Instant fimDoMes = mesDeReferencia.atEndOfMonth().atTime(23, 59, 59).atZone(FUSO_HORARIO_NEGOCIO).toInstant();

        long consultasDoMes = agendamentoRepository.countByHorarioBetween(inicioDoMes, fimDoMes);

        return new KpiDTO(beneficiosPendentes, solicitacoesRecusadas, solicitacoesPendenteAssinatura, consultasPendentes, consultasDoMes);
    }

    private List<ConsultasPorMesDTO> calcularConsultasPorMes(int ano) {
        List<Map<String, Object>> results = agendamentoRepository.countConsultasPorMes(ano);
        return results.stream()
                .map(item -> new ConsultasPorMesDTO(
                        ((String) item.get("mes")).trim(),
                        ((Number) item.get("numero_mes")),
                        ((Number) item.get("quantidade")).longValue()
                ))
                .collect(Collectors.toList());
    }

    private List<SolicitacoesPorBeneficioDTO> calcularSolicitacoesPorBeneficio() {
        List<BeneficioCountDTO> results = solicitacaoRepository.countSolicitacoesPorTipoBeneficio();
        return results.stream()
                .map(item -> new SolicitacoesPorBeneficioDTO(
                        (String) item.tipo(),
                        (long) item.quantidade()
                ))
                .collect(Collectors.toList());
    }
}