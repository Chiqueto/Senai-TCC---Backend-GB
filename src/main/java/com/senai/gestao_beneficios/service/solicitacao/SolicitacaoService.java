package com.senai.gestao_beneficios.service.solicitacao;

import com.senai.gestao_beneficios.DTO.colaborador.ParcelaAbertaDTO;
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
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

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

        Page<SolicitacaoResponseDTO> solicitacoesDtoPage = solicitacoesPage
                .map(solicitacao -> solicitacaoMapper.toDTO(solicitacao)); // Supondo que você tenha um construtor que aceite a entidade

        return solicitacoesDtoPage;
    }

    public Page<SolicitacaoResponseDTO> buscarSolicitacoesPorColaborador(Pageable pageable, String colaboradorId){

        Page<Solicitacao> solicitacoesPage = pageRepository.findByColaboradorId(colaboradorId, pageable);

        Page<SolicitacaoResponseDTO> solicitacoesDtoPage = solicitacoesPage
                .map(solicitacao -> solicitacaoMapper.toDTO(solicitacao)); // Supondo que você tenha um construtor que aceite a entidade

        return solicitacoesDtoPage;
    }

    public ApiResponse<List<ParcelaAbertaDTO>> buscarParcelasAbertasPorColaborador(String colaboradorId) {
        System.out.println("Entrou hahahah");
        List<Solicitacao> solicitacoesAprovadas = repository
                .findByColaboradorIdAndStatusAndTipoPagamento(
                        colaboradorId,
                        StatusSolicitacao.APROVADA,
                        TipoPagamento.DESCONTADO_FOLHA
                );


        List<ParcelaAbertaDTO> parcelasAbertas = new ArrayList<>();
        LocalDate hoje = LocalDate.now(FUSO_HORARIO_NEGOCIO);

        // --- 2. LOOP PARA CALCULAR AS PARCELAS DE CADA SOLICITAÇÃO ---
        for (Solicitacao solicitacao : solicitacoesAprovadas) {

            // Calcula o valor de uma única parcela
            BigDecimal valorParcela = solicitacao.getValorTotal()
                    .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP);

            // Converte a data da solicitação (que está em UTC) para a data local do negócio
            LocalDate dataInicio = solicitacao.getDataSolicitacao().atZone(FUSO_HORARIO_NEGOCIO).toLocalDate();

            // Loop para "gerar" cada parcela virtualmente
            for (int i = 1; i <= solicitacao.getQtdeParcelas(); i++) {

                // Calcula a data de vencimento da parcela atual
                // Assumindo que a primeira parcela vence 1 mês após a solicitação
                LocalDate dataVencimento = dataInicio.plusMonths(i).withDayOfMonth(5);

                // --- A LÓGICA PRINCIPAL: VERIFICA SE A PARCELA ESTÁ "ABERTA" ---
                // Uma parcela está aberta se sua data de vencimento é hoje ou no futuro.
                if (!dataVencimento.isBefore(hoje)) {

                    // Cria o DTO para a parcela aberta e adiciona à lista de resposta
                    ParcelaAbertaDTO parcelaDTO = new ParcelaAbertaDTO(
                            solicitacao.getId(),
                            solicitacao.getBeneficio().getNome(),
                            String.format("%d de %d", i, solicitacao.getQtdeParcelas()),
                            valorParcela,
                            dataVencimento
                    );
                    parcelasAbertas.add(parcelaDTO);
                }
            }
        }

        // --- 3. RETORNO DA RESPOSTA ---
        return new ApiResponse<>(true, parcelasAbertas, null, null, "Parcelas abertas recuperadas com sucesso.");
    }

}

