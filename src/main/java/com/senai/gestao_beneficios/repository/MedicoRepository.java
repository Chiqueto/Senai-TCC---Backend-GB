package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.medico.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, String> {
    Optional<Medico> findByEmail(String email);
}
