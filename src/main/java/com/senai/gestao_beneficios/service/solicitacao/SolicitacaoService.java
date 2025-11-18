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
import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.documento.TipoDocumento;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import com.senai.gestao_beneficios.infra.exceptions.BadRequest;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.infra.exceptions.UnauthorizedException;
import com.senai.gestao_beneficios.repository.*;
import com.senai.gestao_beneficios.service.documento.B2Service;
import com.senai.gestao_beneficios.service.documento.DocumentoGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import static com.senai.gestao_beneficios.specifications.SolicitacaoSpecification.*;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {
    final SolicitacaoRepository repository;
    final PageSolicitacaoRepository pageRepository;
    final ColaboradorRepository colaboradorRepository;
    final DependenteRepository dependenteRepository;
    final BeneficioRepository beneficioRepository;
    final SolicitacaoMapper solicitacaoMapper;
    final DocumentoGenerationService documentoGenerationService;
    final B2Service b2Service;
    final DocumentoRepository documentoRepository;

    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    public ApiResponse<SolicitacaoResponseDTO> criarSolicitacao(SolicitacaoRequestDTO request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Colaborador userLogado = (Colaborador) authentication.getPrincipal();

        Colaborador colaborador = colaboradorRepository.findById(request.idColaborador()).orElseThrow(
                () -> new NotFoundException("colaborador", "Colaborador não encontrado"));

        if (!colaborador.getId().equals(userLogado.getId())) {
            throw new UnauthorizedException("Você não tem permissão para criar agendamentos para outros colaboradores.");
        }

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
        solicitacao.setDesconto(request.valorTotal().multiply(new BigDecimal(beneficio.getPercentualDesconto())).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
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

    public Page<SolicitacaoResponseDTO> buscarTodasAsSolicitacoes(String colaboradorId,
                                                                  StatusSolicitacao status,
                                                                  LocalDate mes,
                                                                  LocalDate dia,
                                                                  Pageable pageable){

        Specification<Solicitacao> spec = Specification
                .allOf(comColaboradorId(colaboradorId))
                .and(comStatus(status))
                .and(doMes(mes))
                .and(comData(dia));

        Page<Solicitacao> solicitacoesPage = pageRepository.findAll(spec, pageable);

        return solicitacoesPage.map(solicitacaoMapper::toDTO);
    }

    public Page<SolicitacaoResponseDTO> buscarSolicitacoesPorColaborador(String colaboradorId,
                                                                         StatusSolicitacao status,
                                                                         LocalDate mes,
                                                                         LocalDate dia,
                                                                         Pageable pageable){
        Specification<Solicitacao> spec = Specification
                .allOf(comColaboradorId(colaboradorId))
                .and(comStatus(status))
                .and(doMes(mes))
                .and(comData(dia));

        Page<Solicitacao> solicitacoesPage = pageRepository.findAll(spec, pageable);

        return solicitacoesPage.map(solicitacaoMapper::toDTO);
    }

    public ApiResponse<List<ParcelaAbertaDTO>> buscarParcelasAbertasPorColaborador(String colaboradorId) {
        List<Solicitacao> solicitacoesAprovadas = repository
                .findByColaboradorIdAndStatusAndTipoPagamento(
                        colaboradorId,
                        StatusSolicitacao.APROVADA,
                        TipoPagamento.DESCONTADO_FOLHA
                );


        List<ParcelaAbertaDTO> parcelasAbertas = new ArrayList<>();
        LocalDate hoje = LocalDate.now(FUSO_HORARIO_NEGOCIO);

        for (Solicitacao solicitacao : solicitacoesAprovadas) {

            BigDecimal valorParcela = solicitacao.getValorTotal()
                    .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP).subtract(solicitacao.getDesconto().divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP));

            LocalDate dataInicio = solicitacao.getDataSolicitacao().atZone(FUSO_HORARIO_NEGOCIO).toLocalDate();

            for (int i = 1; i <= solicitacao.getQtdeParcelas(); i++) {

                LocalDate dataVencimento = dataInicio.plusMonths(i).withDayOfMonth(5);

                if (!dataVencimento.isBefore(hoje)) {

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

        return new ApiResponse<>(true, parcelasAbertas, null, null, "Parcelas abertas recuperadas com sucesso.");
    }

    public ApiResponse<SolicitacaoResponseDTO> aprovarSolicitacao(String idSolicitacao) {
        Solicitacao solicitacao = repository.findById(idSolicitacao)
                .orElseThrow(() -> new NotFoundException("solicitacao", "Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new BadRequest("Apenas solicitações pendentes podem ser aprovadas.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Colaborador gestorLogado = (Colaborador) authentication.getPrincipal();
        String nomeGestor = gestorLogado.getNome();

        documentoGenerationService.gerarDocumentosDeAprovacao(solicitacao, nomeGestor);

        if(solicitacao.getTipoPagamento().equals(TipoPagamento.DESCONTADO_FOLHA)){
            solicitacao.setStatus(StatusSolicitacao.PENDENTE_ASSINATURA);
        }else{
            solicitacao.setStatus(StatusSolicitacao.APROVADA);
        }

        Solicitacao solicitacaoFinal = repository.save(solicitacao);

        return new ApiResponse<>(true, solicitacaoMapper.toDTO(solicitacaoFinal), null, null, "Solicitação aprovada com sucesso!");
    }


    public ApiResponse<SolicitacaoResponseDTO> assinarSolicitacao(String idSolicitacao) {
        Solicitacao solicitacao = repository.findById(idSolicitacao)
                .orElseThrow(() -> new NotFoundException("solicitacao", "Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE_ASSINATURA) {
            throw new BadRequest("Apenas solicitações que estão com assinatura pendente podem ser aprovadas.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Colaborador userLogado = (Colaborador) authentication.getPrincipal();
        String nomeColaborador = userLogado.getNome();

        if (!solicitacao.getColaborador().getId().equals(userLogado.getId())) {
            throw new UnauthorizedException("Você não tem permissão para assinar esta solicitação.");
        }

        Documento reciboNaoAssinado = solicitacao.getDocumentos().stream()
                .filter(doc -> doc.getTipo() == TipoDocumento.RECIBO)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("documento", "Recibo de ciência não encontrado para esta solicitação."));

        String nomeArquivoAntigo = reciboNaoAssinado.getNomeArquivoUnico();

        b2Service.deletarArquivo(nomeArquivoAntigo);

        documentoGenerationService.assinarDocumento(solicitacao, nomeColaborador);

       documentoRepository.delete(reciboNaoAssinado);

        solicitacao.getDocumentos().remove(reciboNaoAssinado);

        solicitacao.setStatus(StatusSolicitacao.APROVADA);

        Solicitacao solicitacaoFinal = repository.save(solicitacao);

        return new ApiResponse<>(true, solicitacaoMapper.toDTO(solicitacaoFinal), null, null, "Solicitação assinada com sucesso!");
    }


}

