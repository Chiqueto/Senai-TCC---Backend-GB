package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, String> {
    Optional<Colaborador> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
}