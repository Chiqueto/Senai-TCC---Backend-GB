package com.senai.gestao_beneficios.service;

import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ColaboradorUserDetailsService implements UserDetailsService {
    private final ColaboradorRepository repo;
    public ColaboradorUserDetailsService(ColaboradorRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        return repo.findByMatricula(matricula)
                .orElseThrow(() -> new UsernameNotFoundException("Colaborador não encontrado"));
    }
}

@Configuration
class PasswordConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}