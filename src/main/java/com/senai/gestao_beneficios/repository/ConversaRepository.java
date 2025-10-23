package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.conversa.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversaRepository extends JpaRepository<com.senai.gestao_beneficios.domain.conversa.Conversa, String> {
}
