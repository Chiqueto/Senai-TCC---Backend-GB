package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.dependente.Dependente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DependenteRepository extends JpaRepository<Dependente, String> {
    Optional<Dependente> findByIdAndColaboradorId(String id, String colaboradorId);
}
