package com.senai.gestao_beneficios.service.solicitacao;

import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoMapper;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoRequestDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoStatusChangeDTO;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {
    final SolicitacaoRepository repository;
    final PageSolicitacaoRepository pageRepository;
    final ColaboradorRepository colaboradorRepository;
    final DependenteRepository dependenteRepository;
    final BeneficioRepository beneficioRepository;
    final SolicitacaoMapper solicitacaoMapper;

    public ApiResponse<SolicitacaoResponseDTO> criarSolicitacao(SolicitacaoRequestDTO request){
        Colaborador colaborador = colaboradorRepository.findById(request.idColaborador()).orElseThrow(
                () -> new NotFoundException("colaborador", "Colaborador não encontrado"));

        Beneficio beneficio = beneficioRepository.findById(request.idBeneficio()).orElseThrow(
                () -> new NotFoundException("beneficio", "Benefício não encontrado"));

        Dependente dependente = null;

        if(request.idDependente() != null) {
            dependente = dependenteRepository.findByIdAndColaboradorId(request.idDependente(), request.idColaborador()).orElseThrow(
                    () -> new NotFoundException("dependente", "Dependente não encontrado para esse colaborador")
            );
        }

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setColaborador(colaborador);
        solicitacao.setBeneficio(beneficio);
        solicitacao.setDescricao(solicitacao.descricao);
        solicitacao.setDataSolicitacao(Instant.now());
        solicitacao.setDependente(dependente);
        solicitacao.setDesconto(request.valorTotal().subtract(request.valorTotal().multiply(new BigDecimal(beneficio.percentualDesconto / 100))));
        solicitacao.setDescricao(request.descricao());
        solicitacao.setTipoPagamento(request.tipoPagamento());
        solicitacao.setQtdeParcelas(request.qtdeParcelas());
        solicitacao.setValorTotal(request.valorTotal());
        solicitacao.setStatus(StatusSolicitacao.PENDENTE);

        Solicitacao savedSolicitacao = repository.save(solicitacao);

        solicitacaoMapper.toDTO(savedSolicitacao);



        return new ApiResponse<SolicitacaoResponseDTO>(true, solicitacaoMapper.toDTO(savedSolicitacao), null, null, "Solicitacao criada com sucesso!");
    }

    public ApiResponse<SolicitacaoResponseDTO> alterarStatus(String idSolicitacao, SolicitacaoStatusChangeDTO dto){
        Solicitacao solicitacao = repository.findById(idSolicitacao).orElseThrow(
                () -> new NotFoundException("solicitacao", "Solicitação não encontrada"));

        solicitacao.setStatus(dto.status());

        Solicitacao statusChangedSolicitacao = repository.save(solicitacao);

        return new ApiResponse<SolicitacaoResponseDTO>(true, solicitacaoMapper.toDTO(statusChangedSolicitacao), null, null, "Status alterado com sucesso!");
    }

    public Page<SolicitacaoResponseDTO> buscarTodasAsSolicitacoes(Pageable pageable){

        Page<Solicitacao> solicitacoesPage = pageRepository.findAll(pageable);

        // 2. Mapeia a página de entidades para uma página de DTOs
        // O método .map() do objeto Page facilita muito essa conversão
        Page<SolicitacaoResponseDTO> solicitacoesDtoPage = solicitacoesPage
                .map(solicitacao -> solicitacaoMapper.toDTO(solicitacao)); // Supondo que você tenha um construtor que aceite a entidade

        return solicitacoesDtoPage;
    }

    public Page<SolicitacaoResponseDTO> buscarSolicitacoesPorColaborador(Pageable pageable, String colaboradorId){

        Page<Solicitacao> solicitacoesPage = pageRepository.findByColaboradorId(colaboradorId, pageable);

        // 2. Mapeia a página de entidades para uma página de DTOs
        // O método .map() do objeto Page facilita muito essa conversão
        Page<SolicitacaoResponseDTO> solicitacoesDtoPage = solicitacoesPage
                .map(solicitacao -> solicitacaoMapper.toDTO(solicitacao)); // Supondo que você tenha um construtor que aceite a entidade

        return solicitacoesDtoPage;
    }

}

