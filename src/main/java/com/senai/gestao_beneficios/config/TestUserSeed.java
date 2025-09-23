package com.senai.gestao_beneficios.config;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Remova a linha abaixo se ela existir, pois não é a anotação correta
// import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional; // ✨ IMPORT CORRETO
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class TestUserSeed {
/*
    @Bean
    @Transactional // ✨ ADICIONE OU VERIFIQUE ESTA ANOTAÇÃO
    ApplicationRunner seedUser(ColaboradorRepository repo) { // Removi o PasswordEncoder, pois não é mais usado aqui
        return args -> {
            final String matricula = "41534";

            Optional<Colaborador> colaboradorOpt = repo.findByMatricula(matricula);

            if (colaboradorOpt.isEmpty()) {
                System.out.println("[SEED] Usuário de teste com matrícula " + matricula + " não encontrado.");
                return;
            }

            Colaborador colaborador = colaboradorOpt.get();


            System.out.println("[SEED] Adicionando dependentes ao usuário existente: " + matricula);

            Dependente dep1 = new Dependente();
            dep1.setNome("Filho(a) de Teste");
            dep1.setColaborador(colaborador);

            Dependente dep2 = new Dependente();
            dep2.setNome("Cônjuge de Teste");
            dep2.setColaborador(colaborador);

            Set<Dependente> dependentes = new HashSet<>();
            dependentes.add(dep1);
            dependentes.add(dep2);
            colaborador.setDependentes(dependentes);

            repo.save(colaborador);

            System.out.println("[SEED] 2 dependentes foram adicionados com sucesso ao usuário " + matricula);
        };
    }

 */
}