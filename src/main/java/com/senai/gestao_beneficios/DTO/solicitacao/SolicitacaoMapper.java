package com.senai.gestao_beneficios.DTO.solicitacao;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioMapper;
import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.DTO.disponibilidade.DisponibilidadeResponseDTO;
import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SolicitacaoMapper {
    public SolicitacaoResponseDTO toDTO(Solicitacao solicitacao) {
        if (solicitacao == null) {
            return null;
        }

        BeneficioMapper beneficioMapper = new BeneficioMapper();


        //EspecialidadeDTO especialidadeDTO = new EspecialidadeDTO(medico.getEspecialidade().getId(), medico.getEspecialidade().getNome());
        ColaboradorDTO colaboradorDTO = new ColaboradorDTO(solicitacao.getColaborador().getId(), solicitacao.getColaborador().getNome(),
                solicitacao.getColaborador().getMatricula(), solicitacao.getColaborador().getDtNascimento(), solicitacao.getColaborador().getFuncao(),
                solicitacao.getColaborador().getGenero(), solicitacao.getColaborador().getCidade(), null);

        DependenteDTO dependenteDTO = solicitacao.getDependente() == null
                ? null
                : new DependenteDTO(solicitacao.getDependente().getId(), solicitacao.getDependente().getNome());

        BeneficioResponseDTO beneficioResponseDTO = beneficioMapper.toDTO(solicitacao.getBeneficio());

        return new SolicitacaoResponseDTO(
                solicitacao.getId(),
                colaboradorDTO,
                dependenteDTO,
                beneficioResponseDTO,
                solicitacao.getValorTotal(),
                solicitacao.getDesconto(),
                solicitacao.getDescricao(),
                solicitacao.getQtdeParcelas(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getTipoPagamento(),
                solicitacao.getStatus()
        );
    }


    public List<SolicitacaoResponseDTO> toDTOList(List<Solicitacao> solicitacoes) {
        return solicitacoes.stream().map(this::toDTO).collect(Collectors.toList());
    }

}
