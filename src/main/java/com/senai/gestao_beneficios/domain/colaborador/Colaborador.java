package com.senai.gestao_beneficios.domain.colaborador;

import com.senai.gestao_beneficios.domain.dependente.Dependente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "colaborador")
@RequiredArgsConstructor

public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String matricula;
    @Column(nullable = false)
    private String senha;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private LocalDate dtNascimento;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Funcao funcao;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genero genero;
    private String cidade;
    private Instant created_at;
    private Instant updated_at;
@OneToMany(
        orphanRemoval = true,
        mappedBy = "colaborador",
        cascade = CascadeType.ALL
)
    private Set<Dependente> dependentes;
}
