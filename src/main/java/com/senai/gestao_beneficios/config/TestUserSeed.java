package com.senai.gestao_beneficios.config;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.colaborador.Genero;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
// Remova a linha abaixo se ela existir, pois não é a anotação correta
// import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; // ✨ IMPORT CORRETO

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
/*
@Configuration
public class TestUserSeed {
/*
    @Bean
    @Transactional // ✨ ADICIONE OU VERIFIQUE ESTA ANOTAÇÃO
    ApplicationRunner seedUser(ColaboradorRepository repo) { // Removi o PasswordEncoder, pois não é mais usado aqui
        return args -> {
            final String matricula = "41532";

            Optional<Colaborador> colaboradorOpt = repo.findByMatricula(matricula);

            if (colaboradorOpt.isEmpty()) {
                System.out.println("[SEED] Usuário de teste com matrícula " + matricula + " não encontrado.");
                return;
            }

            Colaborador colaborador = colaboradorOpt.get();


            System.out.println("[SEED] Adicionando dependentes ao usuário existente: " + matricula);

            Dependente dep1 = new Dependente();
            dep1.setNome("Juquinha");
            dep1.setColaborador(colaborador);

            Dependente dep2 = new Dependente();
            dep2.setNome("Marina");
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
/*
@Bean
@Transactional
ApplicationRunner seedNewUser(ColaboradorRepository repo, PasswordEncoder passwordEncoder) {
    return args -> {
        // Defina a matrícula para o novo usuário de teste
        final String novaMatricula = "41534";

        // 1. Verifica se o colaborador já existe
        Optional<Colaborador> colaboradorExistente = repo.findByMatricula(novaMatricula);

        if (colaboradorExistente.isPresent()) {
            System.out.println("[SEED] Usuário com matrícula " + novaMatricula + " já existe. Nenhum novo usuário foi criado.");
        } else {
            System.out.println("[SEED] Criando um novo usuário de teste com matrícula: " + novaMatricula);

            // 2. Cria a nova instância do Colaborador
            Colaborador novoColaborador = new Colaborador();

            // 3. Preenche os dados do novo colaborador
            novoColaborador.setMatricula(novaMatricula);
            novoColaborador.setNome("Luís Felipe");
            novoColaborador.setMatricula("41534");
            String senhaPlana = "123456";
            String senhaCriptografada = passwordEncoder.encode(senhaPlana);
            novoColaborador.setSenha(senhaCriptografada); // ✨ Lembre-se de criptografar a senha em um ambiente real!
            novoColaborador.setDtNascimento(LocalDate.of(2005, 3, 8));
            novoColaborador.setCidade("São Joaquim da Barra");
            novoColaborador.setFuncao(Funcao.GESTAO_BENEFICIOS);
            novoColaborador.setGenero(Genero.MASCULINO);
            novoColaborador.setCreated_at(Instant.now());
            novoColaborador.setUpdated_at(Instant.now());

            // 4. Salva o novo colaborador no banco de dados
            repo.save(novoColaborador);

            System.out.println("[SEED] Novo usuário de teste com matrícula " + novaMatricula + " criado com sucesso!");
        }
    };
}
/*
}
 */