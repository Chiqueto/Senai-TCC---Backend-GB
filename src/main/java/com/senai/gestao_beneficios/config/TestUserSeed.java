package com.senai.gestao_beneficios.config;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.colaborador.Genero;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class TestUserSeed {

    @Bean
    ApplicationRunner seedUser(ColaboradorRepository repo, PasswordEncoder encoder) {
        return args -> {
            final String matricula = "41534";
            final String rawPassword = "senha123"; // <- use essa no login

            if (repo.existsByMatricula(matricula)) {
                System.out.println("[SEED] Usuário de teste já existe: " + matricula);
                return;
            }

            Colaborador c = new Colaborador();
            c.setMatricula(matricula);
            c.setSenha(encoder.encode(rawPassword));
            c.setNome("Usuário de Teste");
            c.setDtNascimento(LocalDate.of(1995, 1, 1));
            c.setFuncao(Funcao.GESTAO_BENEFICIOS);
            c.setGenero(Genero.MASCULINO);
            c.setCidade("São Paulo");

            repo.save(c);
            System.out.println("[SEED] Criado usuário -> matricula=" + matricula + " | senha=" + rawPassword);
        };
    }
}