package com.senai.gestao_beneficios.specifications;


import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class SolicitacaoSpecification {

    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    /**
     * Cria um filtro para buscar por ID de colaborador.
     */
    public static Specification<Solicitacao> comColaboradorId(String colaboradorId) {
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
    public static Specification<Solicitacao> comStatus(StatusSolicitacao status) {
        return (root, query, builder) -> {
            if (status == null) {
                return null;
            }
            return builder.equal(root.get("status"), status);
        };
    }

    /**
     * Cria um filtro para buscar solicitações dentro de um mês e ano específicos.
     */
    public static Specification<Solicitacao> doMes(LocalDate mes) {
        return (root, query, builder) -> {
            if (mes == null) {
                return null;
            }
            // Calcula o início do mês em UTC
            Instant inicioDoMes = mes.withDayOfMonth(1)
                    .atStartOfDay(FUSO_HORARIO_NEGOCIO)
                    .toInstant();

            // Calcula o início do próximo mês em UTC
            Instant inicioDoProximoMes = mes.with(TemporalAdjusters.firstDayOfNextMonth())
                    .atStartOfDay(FUSO_HORARIO_NEGOCIO)
                    .toInstant();

            // Retorna a condição: horario >= inicioDoMes E horario < inicioDoProximoMes
            return builder.between(root.get("dataSolicitacao"), inicioDoMes, inicioDoProximoMes);
        };
    }

    public static Specification<Solicitacao> comData(LocalDate data) {
        return (root, query, builder) -> {
            if (data == null) {
                return null;
            }

            Instant inicioDoDia = data.atStartOfDay(FUSO_HORARIO_NEGOCIO).toInstant();

            Instant fimDoDia = data.plusDays(1).atStartOfDay(FUSO_HORARIO_NEGOCIO).toInstant();

            return builder.between(root.get("dataSolicitacao"), inicioDoDia, fimDoDia);
        };
    }
}