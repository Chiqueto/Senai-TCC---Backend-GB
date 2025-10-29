package com.senai.gestao_beneficios.service.documento;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.senai.gestao_beneficios.DTO.solicitacao.ParcelaInfo;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
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
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocumentoGenerationService {

    private final TemplateEngine templateEngine;
    private final B2Service b2Service;
    private final DocumentoRepository documentoRepository;

    @Value("classpath:static/fonts/DancingScript-Regular.ttf")
    private Resource dancingScriptFont;

    private void prepararRenderer(ITextRenderer renderer) throws IOException, DocumentException {
        ITextFontResolver fontResolver = renderer.getFontResolver();

        // 🔹 Primeiro tenta carregar do classpath (funciona dentro do JAR)
        try {
            URL fontUrl = getClass().getResource("/static/fonts/DancingScript-Regular.ttf");
            if (fontUrl != null) {
                fontResolver.addFont(fontUrl.toExternalForm(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                System.out.println("Fonte DancingScript carregada de /static/fonts/");
            } else {
                // 🔹 Fallback: tenta do classpath raiz (caso esteja em /resources/fonts)
                Resource fallbackFont = dancingScriptFont;
                fontResolver.addFont(fallbackFont.getFile().getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                System.out.println("Fonte DancingScript carregada de /fonts/");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Não foi possível carregar a fonte DancingScript: " + e.getMessage());
        }

        // 🔹 Ajusta contexto do renderer
        renderer.getSharedContext().setReplacedElementFactory(
                renderer.getSharedContext().getReplacedElementFactory()
        );
    }

    private String obterBaseUrlStatic() {
        // Aponta para /static/ no classpath (funciona empacotado em JAR também)
        // Ex.: .../BOOT-INF/classes!/static/
        return Objects.requireNonNull(getClass().getResource("/static/")).toExternalForm();
    }

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


            gerarESalvarPdf(solicitacao, TipoDocumento.AUTORIZACAO, "autorizacao.html", authContext, true);

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

                reciboContext.setVariable("parcelas", parcelas);

                gerarESalvarPdf(solicitacao, TipoDocumento.RECIBO, "recibo.html", reciboContext, false);
            }
        } catch (Exception e) {
            throw new ServerException("Falha ao gerar documentos de aprovação: " + e.getMessage());
        }
    }

    public void assinarDocumento(Solicitacao solicitacao, String nomeColaborador) {
        try {
            Context authContext = new Context();
            authContext.setVariable("solicitacao", solicitacao);
            authContext.setVariable("colaborador", solicitacao.getColaborador());
            authContext.setVariable("nomeGestor", nomeColaborador);
            ZoneId fusoHorario = ZoneId.of("America/Sao_Paulo");

            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("pt", "BR"));
            authContext.setVariable("dataAutorizacao", LocalDate.now().format(formatter));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            authContext.setVariable("horaAutorizacao", ZonedDateTime.now(fusoHorario).format(timeFormatter));

            if (solicitacao.getTipoPagamento() == TipoPagamento.DESCONTADO_FOLHA) {
                Context reciboContext = new Context();
                reciboContext.setVariable("solicitacao", solicitacao);
                reciboContext.setVariable("colaborador", solicitacao.getColaborador());

                List<ParcelaInfo> parcelas = new ArrayList<>();
                LocalDate dataInicio = solicitacao.getDataSolicitacao().atZone(fusoHorario).toLocalDate();
                DateTimeFormatter parcelasFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                BigDecimal valorParcela = solicitacao.getValorTotal()
                        .divide(new BigDecimal(solicitacao.getQtdeParcelas()), 2, RoundingMode.HALF_UP);
                String valorParcelaFormatado = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valorParcela);

                for (int i = 1; i <= solicitacao.getQtdeParcelas(); i++) {
                    LocalDate dataVencimento = dataInicio.plusMonths(i).withDayOfMonth(5);
                    parcelas.add(new ParcelaInfo(
                            String.valueOf(i),
                            dataVencimento.format(parcelasFormatter),
                            valorParcelaFormatado
                    ));
                }

                reciboContext.setVariable("parcelas", parcelas);

                gerarESalvarPdf(solicitacao, TipoDocumento.RECIBO, "recibo.html", reciboContext, true);
            }
        } catch (Exception e) {
            throw new ServerException("Falha ao assinar documento: " + e.getMessage());
        }
    }

    private void gerarESalvarPdf(Solicitacao solicitacao, TipoDocumento tipo, String templateNome, Context context, boolean incluirAssinatura)
            throws IOException, DocumentException {

        context.setVariable("incluirAssinatura", incluirAssinatura); // 🔹 envia a flag ao template

        String html = templateEngine.process(templateNome, context);

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            prepararRenderer(renderer);

            String baseUrl = obterBaseUrlStatic();
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(pdfStream);

            byte[] pdfBytes = pdfStream.toByteArray();

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


    public byte[] gerarPdfAutorizacaoParaTeste() throws IOException, DocumentException {

        // --- 1. CRIA DADOS FALSOS (MOCK) ---
        Colaborador colaboradorMock = new Colaborador();
        colaboradorMock.setNome("Maria Eduarda Santos Pereira");
        colaboradorMock.setMatricula("41535");
        colaboradorMock.setFuncao(Funcao.OUTRO); // Use um valor do seu Enum

        Beneficio beneficioMock = new Beneficio();
        beneficioMock.setNome("Exemplo de Benefício Autorizado");

        Solicitacao solicitacaoMock = new Solicitacao();
        solicitacaoMock.setId("uuid-de-teste-123456789");
        solicitacaoMock.setColaborador(colaboradorMock);
        solicitacaoMock.setBeneficio(beneficioMock);
        solicitacaoMock.setDescricao("Viagem para teste de layout do documento.");

        String nomeGestorMock = "Nome do Gestor de Teste";

        // --- 2. PREENCHE O CONTEXTO (igual à lógica de aprovação) ---
        Context authContext = new Context();
        authContext.setVariable("solicitacao", solicitacaoMock);
        authContext.setVariable("colaborador", solicitacaoMock.getColaborador());
        authContext.setVariable("nomeGestor", nomeGestorMock);
        ZoneId fusoHorario = ZoneId.of("America/Sao_Paulo");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        authContext.setVariable("dataAutorizacao", ZonedDateTime.now(fusoHorario).format(dateFormatter));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        authContext.setVariable("horaAutorizacao", ZonedDateTime.now(fusoHorario).format(timeFormatter));

        // --- 3. CHAMA O GERADOR DE PDF E RETORNA OS BYTES ---
        return gerarPdfBytes("autorizacao.html", authContext);
    }

    private byte[] gerarPdfBytes(String templateNome, Context context) throws IOException, DocumentException {
        String html = templateEngine.process(templateNome, context);
        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            prepararRenderer(renderer);

            String baseUrl = obterBaseUrlStatic();
            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(pdfStream);
            return pdfStream.toByteArray();
        }
    }

    public byte[] gerarPdfReciboParaTeste() throws IOException, DocumentException {

        // --- Mock de dados ---
        Colaborador colaboradorMock = new Colaborador();
        colaboradorMock.setNome("Kaiky Botelho");
        colaboradorMock.setMatricula("41532");
        colaboradorMock.setFuncao(Funcao.OUTRO);

        Solicitacao solicitacaoMock = new Solicitacao();
        solicitacaoMock.setId("uuid-de-teste-987654321");
        solicitacaoMock.setColaborador(colaboradorMock);
        solicitacaoMock.setValorTotal(new BigDecimal("1200.00"));
        solicitacaoMock.setDataSolicitacao(LocalDate.now().atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant());

        // --- Mock das Parcelas (ESTAVA FALTANDO) ---
        solicitacaoMock.setQtdeParcelas(2); // Defina o número de parcelas

        List<ParcelaInfo> parcelasMock = new ArrayList<>();
        // Assumindo que seu ParcelaInfo seja (numero, data, valor)
        parcelasMock.add(new ParcelaInfo("1", "05/11/2025", "600,00"));
        parcelasMock.add(new ParcelaInfo("2", "05/12/2025", "600,00"));

        // --- Contexto Thymeleaf ---
        Context context = new Context();
        context.setVariable("solicitacao", solicitacaoMock);
        context.setVariable("colaborador", colaboradorMock);
        context.setVariable("parcelas", parcelasMock); // Adiciona a lista ao contexto
        context.setVariable("incluirAssinatura", true); // Para mostrar a assinatura no teste

        // --- Geração do PDF ---
        return gerarPdfBytes("recibo.html", context);
    }

}