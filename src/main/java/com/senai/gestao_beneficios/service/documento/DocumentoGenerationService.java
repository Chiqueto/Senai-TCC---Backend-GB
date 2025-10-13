package com.senai.gestao_beneficios.service.documento;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
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
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
            // Prepara os dados para o template de AUTORIZAÇÃO
            Context authContext = new Context();
            authContext.setVariable("solicitacao", solicitacao);
            authContext.setVariable("colaborador", solicitacao.getColaborador());
            authContext.setVariable("nomeGestor", nomeGestor);
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

                // CORREÇÃO DO BUG: Usando o Enum correto
                gerarESalvarPdf(solicitacao, TipoDocumento.RECIBO, "recibo.html", reciboContext);
            }
        } catch (Exception e) {
            // Envelopa a exceção para um tratamento padronizado no GlobalExceptionHandler
            throw new ServerException("Falha ao gerar documentos de aprovação: " + e.getMessage());
        }
    }

    // Removido @SneakyThrows para um tratamento de exceção explícito
    private void gerarESalvarPdf(Solicitacao solicitacao, TipoDocumento tipo, String templateNome, Context context) throws IOException, DocumentException {
        String html = templateEngine.process(templateNome, context);

        try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // --- A MUDANÇA MAIS IMPORTANTE ESTÁ AQUI ---

            // 1. Encontra a pasta 'resources' (ou 'classes' depois da compilação).
            //    Isso nos dará o caminho base para resolver outros arquivos, como as fontes.
            //    Usamos um arquivo conhecido (application.properties) como ponto de referência.
            String baseUrl = getClass().getClassLoader().getResource("").toString();

            // 2. Passamos o HTML E o baseUrl para o renderizador.
            //    Agora, quando ele ler `url('fonts/...')` no CSS, ele saberá procurar
            //    a partir deste diretório base.
            renderer.setDocumentFromString(html, baseUrl);

            // NÃO PRECISAMOS MAIS DO FONTRESOLVER AQUI, O CSS VAI CUIDAR DE TUDO.
            // ITextFontResolver fontResolver = renderer.getFontResolver();
            // fontResolver.addFont(...) // <--- PODE APAGAR ESTA PARTE

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