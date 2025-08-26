package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.medico.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, String> {
}
