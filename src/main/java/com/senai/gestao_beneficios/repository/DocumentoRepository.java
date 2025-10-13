package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.documento.Documento;
import com.senai.gestao_beneficios.domain.documento.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, String> {
    public List<Documento> findBySolicitacaoId(String solicitacaoId);

    public Optional<Documento> findByNomeArquivoUnico(String nomeArquivoUnico);

    Optional<Documento> findBySolicitacaoIdAndTipo(String solicitacaoId, TipoDocumento tipo);
}
