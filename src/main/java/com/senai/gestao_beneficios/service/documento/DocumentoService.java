package com.senai.gestao_beneficios.service.documento;

import com.senai.gestao_beneficios.DTO.documento.DocumentoMapper;
import com.senai.gestao_beneficios.DTO.documento.DocumentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.repository.DocumentoRepository;
import com.senai.gestao_beneficios.repository.SolicitacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    // Defina o diretório onde os arquivos serão salvos
    private final String diretorioDeUpload = "uploads/";
    private final SolicitacaoRepository solicitacaoRepository;
    final DocumentoRepository repository;
    final DocumentoMapper documentoMapper;
    final ColaboradorRepository colaboradorRepository;

    public String salvarArquivo(MultipartFile file) throws IOException {
        criarDiretorioSeNaoExistir();
        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio ou inválido.");
        }

        String nomeOriginal = StringUtils.cleanPath(file.getOriginalFilename());

        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        String nomeUnico = UUID.randomUUID().toString() + extensao;

        Path caminhoCompleto = Paths.get(diretorioDeUpload + nomeUnico);

        Files.copy(file.getInputStream(), caminhoCompleto);

        return caminhoCompleto.toString();
    }

    private void criarDiretorioSeNaoExistir() {
        File diretorio = new File(diretorioDeUpload);
        if (!diretorio.exists()) {
            diretorio.mkdirs(); // Cria o diretório e qualquer pasta pai necessária
        }
    }

    public ApiResponse<List<DocumentoResponseDTO>> getAllDocumentsBySolicitacao (String idSolicitacao, String idColaborador) {
        Colaborador colaborador = colaboradorRepository.findById(idColaborador).orElseThrow(() -> new NotFoundException("colaborador", "Colaborador não encontrado!"));

        if (colaborador.getFuncao() == Funcao.OUTRO){
            Solicitacao solicitacao = solicitacaoRepository.findByIdAndColaboradorId(idSolicitacao, idColaborador).orElseThrow(() -> new NotFoundException("solicitacao", "solicitacao nao encontrada!"));
        }else {
            Solicitacao solicitacao = solicitacaoRepository.findById(idSolicitacao).orElseThrow(() -> new NotFoundException("solicitacao", "solicitacao nao encontrada!"));
        }

        List<Documento> documentos = repository.findBySolicitacaoId(idSolicitacao);

        List<DocumentoResponseDTO> documentoResponseDTOs = documentoMapper.toDTOList(documentos);

        return new ApiResponse<List<DocumentoResponseDTO>>(true, documentoResponseDTOs, null, null, "Documentos encontrados com sucesso!");
    }
}
