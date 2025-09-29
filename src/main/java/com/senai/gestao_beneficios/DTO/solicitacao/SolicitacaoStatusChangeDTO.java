package com.senai.gestao_beneficios.DTO.solicitacao;

import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import jakarta.validation.constraints.*;

public record SolicitacaoStatusChangeDTO(
        @NotNull(message = "Novo status deve ser informado")
        StatusSolicitacao status
)
{
}
