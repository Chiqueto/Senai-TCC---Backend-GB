package com.senai.gestao_beneficios.service.solicitacao;

import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoRequestDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.BeneficioRepository;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.repository.SolicitacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {
    final SolicitacaoRepository repository;
    final ColaboradorRepository colaboradorRepository;
    final BeneficioRepository beneficioRepository;

    public ApiResponse<SolicitacaoResponseDTO> criarSolicitacao(SolicitacaoRequestDTO request){
        Colaborador colaborador = colaboradorRepository.findById(request.idColaborador()).orElseThrow(
                () -> new NotFoundException("colaborador", "Colaborador não encontrado"));

        Beneficio beneficio = beneficioRepository.findById(request.idBeneficio()).orElseThrow(
                () -> new NotFoundException("beneficio", "Benefício não encontrado"));

        return null;
    }

}

