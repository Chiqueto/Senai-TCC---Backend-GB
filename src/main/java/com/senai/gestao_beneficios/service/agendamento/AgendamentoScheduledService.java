package com.senai.gestao_beneficios.service.agendamento;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import com.senai.gestao_beneficios.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoScheduledService.class);
    private final AgendamentoRepository agendamentoRepository;

    @Scheduled(cron = "0 30 * * * *")
    public void concluirAgendamentosPassados() {
        logger.info("Iniciando tarefa agendada: Concluir Agendamentos Passados...");

        Instant agora = Instant.now();

        List<Agendamento> agendamentosParaConcluir = agendamentoRepository
                .findByStatusAndHorarioBefore(StatusAgendamento.AGENDADO, agora);

        if (agendamentosParaConcluir.isEmpty()) {
            logger.info("Nenhum agendamento para concluir.");
            return;
        }

        logger.info("Encontrados {} agendamentos para atualizar para 'CONCLUIDO'.", agendamentosParaConcluir.size());

        for (Agendamento agendamento : agendamentosParaConcluir) {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        }

        agendamentoRepository.saveAll(agendamentosParaConcluir);

        logger.info("Tarefa finalizada. {} agendamentos foram concluídos.", agendamentosParaConcluir.size());
    }
}
