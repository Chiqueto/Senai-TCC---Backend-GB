package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, String> {
}
