package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.documento.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, String> {
}
