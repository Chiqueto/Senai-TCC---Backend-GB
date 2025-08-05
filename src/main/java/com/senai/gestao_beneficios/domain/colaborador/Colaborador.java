package com.senai.gestao_beneficios.domain.colaborador;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "colaborador")
@RequiredArgsConstructor

public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String matricula;
    private String senha;
    private String nome;
    private LocalDate dtNascimento;
    @Enumerated(EnumType.STRING)
    private Funcao funcao;
    @Enumerated(EnumType.STRING)
    private Genero genero;
    private String cidade;
    private Instant created_at;
    private Instant updated_at;


}
