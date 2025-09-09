package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficioRepository extends JpaRepository<Beneficio, String> {
    public Optional<Beneficio> findByNome(String nome);
}
