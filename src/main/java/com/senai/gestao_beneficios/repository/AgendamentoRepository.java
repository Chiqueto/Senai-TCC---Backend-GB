package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {
    boolean existsByMedicoIdAndHorarioAndStatusNot(String medicoId, Instant horario, StatusAgendamento status);

    List<Agendamento> findByMedicoIdAndHorarioBetween(String medicoId, Instant startOfDay, Instant endOfDay);

    List<Agendamento> findByColaboradorId(String colaboradorId);

    List<Agendamento> findByStatusAndHorarioBefore(StatusAgendamento status, Instant agora);

    long countByStatusAndHorarioAfter(StatusAgendamento status, Instant agora);

    long countByHorarioBetween(Instant inicio, Instant fim);

    @Query(value = "SELECT TO_CHAR(horario, 'Month') as mes, EXTRACT(MONTH FROM horario) as numero_mes, COUNT(*) as quantidade\n" +
            "            FROM agendamentos \n" +
            "            WHERE EXTRACT(YEAR FROM horario) = '2025' \n" +
            "            GROUP BY TO_CHAR(horario, 'Month'), EXTRACT(MONTH FROM horario) \n" +
            "            ORDER BY EXTRACT(MONTH FROM horario)", nativeQuery = true)
    List<Map<String, Object>> countConsultasPorMes(int ano);
}
