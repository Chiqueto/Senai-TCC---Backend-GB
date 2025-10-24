package com.senai.gestao_beneficios.specifications;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;

public class AgendamentoSpecification {
    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    /**
     * Cria um filtro para buscar por ID de colaborador.
     */
    public static Specification<Agendamento> comColaboradorId(String colaboradorId) {
        return (root, query, builder) -> {
            if (colaboradorId == null) {
                return null;
            }
            return builder.equal(root.get("colaborador").get("id"), colaboradorId);
        };
    }

    /**
     * Cria um filtro para buscar por um status específico.
     */
    public static Specification<Agendamento> comStatus(StatusAgendamento status) {
        return (root, query, builder) -> {
            if (status == null) {
                return null;
            }
            return builder.equal(root.get("status"), status);
        };
    }
}
