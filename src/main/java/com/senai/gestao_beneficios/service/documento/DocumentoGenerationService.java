package com.senai.gestao_beneficios.service.documento;

import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.documento.TipoDocumento;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import com.senai.gestao_beneficios.infra.exceptions.ServerException;
import com.senai.gestao_beneficios.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentoGenerationService {

    private final TemplateEngine templateEngine;
    private final B2Service b2Service;
    private final DocumentoRepository documentoRepository;

    public void gerarDocumentosDeAprovacao(Solicitacao solicitacao, String nomeGestor) {
        try {
            // --- MUDANÇA AQUI: PREPARAÇÃO DOS DADOS ---
            // Prepara os dados para o template de AUTORIZAÇÃO
            Context authContext = new Context();
            authContext.setVariable("solicitacao", solicitacao);
            authContext.setVariable("colaborador", solicitacao.getColaborador());
            authContext.setVariable("nomeGestor", nomeGestor); // Nome do gestor que aprovou
            // Formata a data para um formato legível (ex: "13 de outubro de 2025")
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("pt", "BR"));
            authContext.setVariable("dataAutorizacao", LocalDate.now().format(formatter));

            gerarESalvarPdf(solicitacao, TipoDocumento.AUTORIZACAO, "autorizacao.html", authContext);


            // Se for Empréstimo, prepara os dados para o RECIBO
            if (solicitacao.getTipoPagamento() == TipoPagamento.DESCONTADO_FOLHA) {
                Context reciboContext = new Context();
                reciboContext.setVariable("solicitacao", solicitacao);
                reciboContext.setVariable("colaborador", solicitacao.getColaborador());
                BigDecimal valorParcela = solicitacao.getValorTotal()
                        .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP);
                reciboContext.setVariable("valorParcela", valorParcela);

                gerarESalvarPdf(solicitacao, TipoDocumento.RECIBO, "recibo.html", reciboContext);
            }
        } catch (Exception e) {
            throw new ServerException("Falha ao gerar documentos de aprovação: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void gerarESalvarPdf(Solicitacao solicitacao, TipoDocumento tipo, String templateNome, Context context)  {
        // 1. Preenche o template HTML com os dados da solicitação
        context.setVariable("solicitacao", solicitacao);
        context.setVariable("colaborador", solicitacao.getColaborador());
        // Se for o recibo, calcula e adiciona o valor da parcela
        if (tipo == TipoDocumento.RECIBO) {
            BigDecimal valorParcela = solicitacao.getValorTotal()
                    .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP);
            context.setVariable("valorParcela", valorParcela);
        }
        String html = templateEngine.process(templateNome, context);

        // 2. Converte o HTML para PDF em memória
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(pdfStream);
        byte[] pdfBytes = pdfStream.toByteArray();

        // 3. Faz o upload do PDF para o B2 (precisamos de um novo método no B2Service)
        String nomeUnico = b2Service.uploadArquivoGerado(pdfBytes, "application/pdf", tipo.name() + ".pdf");

        // 4. Cria e salva o registro do novo documento no banco
        Documento novoDocumento = new Documento();
        novoDocumento.setSolicitacao(solicitacao);
        novoDocumento.setNomeArquivoUnico(nomeUnico);
        novoDocumento.setNomeArquivoOriginal(tipo.name().toLowerCase() + "_" + solicitacao.getId() + ".pdf");
        novoDocumento.setTipo(tipo);
        novoDocumento.setTamanho(pdfBytes.length);
        novoDocumento.setContentType("application/pdf");
        novoDocumento.setDataUpload(Instant.now());
        documentoRepository.save(novoDocumento);
    }
}