package com.senai.gestao_beneficios.service.documento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentoService {
    // Defina o diretório onde os arquivos serão salvos
    private final String diretorioDeUpload = "uploads/";

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
}
