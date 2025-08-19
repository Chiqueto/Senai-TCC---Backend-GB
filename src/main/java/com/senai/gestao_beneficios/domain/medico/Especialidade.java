package com.senai.gestao_beneficios.domain.medico;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "especialidade")
@RequiredArgsConstructor
@Getter
@Setter
public class Especialidade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String nome;
    @OneToMany(mappedBy = "especialidade")
    private List<Medico> medicos;

}
