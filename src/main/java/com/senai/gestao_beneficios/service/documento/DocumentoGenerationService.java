package com.senai.gestao_beneficios.service.documento;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.senai.gestao_beneficios.DTO.solicitacao.ParcelaInfo;
import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.documento.TipoDocumento;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import com.senai.gestao_beneficios.infra.exceptions.ServerException;
import com.senai.gestao_beneficios.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentoGenerationService {

    private final TemplateEngine templateEngine;
    private final B2Service b2Service;
    private final DocumentoRepository documentoRepository;

    @Value("classpath:fonts/DancingScript-Regular.ttf")
    private Resource dancingScriptFont;

    public void gerarDocumentosDeAprovacao(Solicitacao solicitacao, String nomeGestor) {
        try {
            Context authContext = new Context();
            authContext.setVariable("solicitacao", solicitacao);
            authContext.setVariable("colaborador", solicitacao.getColaborador());
            authContext.setVariable("nomeGestor", nomeGestor);
            ZoneId fusoHorario = ZoneId.of("America/Sao_Paulo");

            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("pt", "BR"));
            authContext.setVariable("dataAutorizacao", LocalDate.now().format(formatter));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            authContext.setVariable("horaAutorizacao", ZonedDateTime.now(fusoHorario).format(timeFormatter));


            gerarESalvarPdf(solicitacao, TipoDocumento.AUTORIZACAO, "autorizacao.html", authContext);

            if (solicitacao.getTipoPagamento() == TipoPagamento.DESCONTADO_FOLHA) {
                Context reciboContext = new Context();
                reciboContext.setVariable("solicitacao", solicitacao);
                reciboContext.setVariable("colaborador", solicitacao.getColaborador());

                // Cria a lista de parcelas para o template
                List<ParcelaInfo> parcelas = new ArrayList<>();
                LocalDate dataInicio = solicitacao.getDataSolicitacao().atZone(fusoHorario).toLocalDate();
                DateTimeFormatter parcelasFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                BigDecimal valorParcela = solicitacao.getValorTotal()
                        .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP);
                // Formata o valor com vírgula para exibir no PDF
                String valorParcelaFormatado = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valorParcela);

                for (int i = 1; i <= solicitacao.getQtdeParcelas(); i++) {
                    LocalDate dataVencimento = dataInicio.plusMonths(i).withDayOfMonth(5); // Usando a regra do dia 5
                    parcelas.add(new ParcelaInfo(
                            String.valueOf(i),
                            dataVencimento.format(parcelasFormatter),
                            valorParcelaFormatado
                    ));
                }
                // Envia a lista de parcelas para o template
                reciboContext.setVariable("parcelas", parcelas);

                // CORREÇÃO DO BUG do Enum
                gerarESalvarPdf(solicitacao, TipoDocumento.RECIBO, "recibo.html", reciboContext);
            }
        } catch (Exception e) {
            throw new ServerException("Falha ao gerar documentos de aprovação: " + e.getMessage());
        }
    }

    // Removido @SneakyThrows para um tratamento de exceção explícito
    private void gerarESalvarPdf(Solicitacao solicitacao, TipoDocumento tipo, String templateNome, Context context) throws IOException, DocumentException {
        String html = templateEngine.process(templateNome, context);

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            String baseUrl = getClass().getClassLoader().getResource("").toString();


            renderer.setDocumentFromString(html, baseUrl);

            renderer.layout();
            renderer.createPDF(pdfStream);
            byte[] pdfBytes = pdfStream.toByteArray();

            // O resto da sua lógica de upload e salvamento
            String nomeUnico = b2Service.uploadArquivoGerado(pdfBytes, "application/pdf", tipo.name() + ".pdf");

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
}